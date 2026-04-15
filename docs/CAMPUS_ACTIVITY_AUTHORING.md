## Campus Activity Authoring

This document describes the current authoring flows implemented for the Campus module when the
runtime needs to create Moodle activities or resources through `course/mod.php` and
`course/modedit.php`.

### Scope

Current flows:

- `createAssignment`
- `createPdfResource`

Both flows depend on an authenticated Moodle browser session that has already been synchronized to
the Java HTTP client through `CampusSessionService`.

### Supported surfaces

Assignment creation is exposed in two places:

- REST: `POST /api/courses/{courseId}/assignments`
- MCP: `createAssignment(courseId, request)`

PDF resource creation is exposed in two places:

- REST: `POST /api/courses/{courseId}/resources/pdf`
- MCP: `createPdfResource(...)`

### Shared transport model

Campus authoring does not call a stable Moodle public REST endpoint for these operations.

Instead the runtime:

1. opens `course/mod.php?id=<courseId>&add=<moduleName>&section=<section>`
2. extracts the real `modedit.php` form fields from the HTML
3. normalizes the subset of fields that must be overridden
4. submits the final `POST` to `course/modedit.php`

This is why the generic gateway methods are now:

- `getCourseModEditForm(courseId, section, moduleName)`
- `postCourseModEdit(params)`

The generic methods are shared by assignment and resource creation so both module types follow the
same Moodle entrypoint.

### Assignment creation contract

Request DTO:

- required: `section`, `name`
- optional:
  - `description`
  - `activityInstructions`
  - `availableFrom`
  - `dueAt`
  - `cutoffAt`
  - `gradingDueAt`
  - `visible`
  - `showDescription`
  - `alwaysShowDescription`
  - `sendNotifications`
  - `sendLateNotifications`
  - `sendStudentNotifications`

Accepted date formats:

- `2026-04-30T23:59`
- `2026-04-30`
- zoned ISO-8601 timestamps

Response DTO:

- `courseId`
- `section`
- `name`
- `assignmentId`
- `assignmentUrl`
- `courseUrl`

### Bug fixed in `createAssignment`

The original failure pattern was a systematic `assign_create_failed` even when the caller changed
HTML, text and date values.

The root cause was not the payload business fields. The problem was the Moodle form serialization:

- the assignment flow uses the real Moodle form, so the serialized payload must match the browser
  submission closely
- empty form fields such as `coursemodule` and `instance` need to be normalized to `0`
- Moodle tag widgets use a hidden field plus a multiselect widget
- the multiselect companion `tags[]` must not overwrite the hidden
  `_qf__force_multiselect_submission` marker
- assignment creation also needs the right expanded-section flags for the assignment form
  sections such as submission types and feedback types

### `MoodleModEditFormFieldExtractor`

The shared extractor is responsible for reading a Moodle `modedit.php` form into a flat map.

Important behavior:

- disabled controls are ignored
- unchecked checkboxes and radios are ignored
- submit/button/file inputs are ignored
- normal selects resolve to the selected option or the first option
- multiselects return `null` when nothing is selected, so empty widgets like `tags[]` are omitted

That last rule is essential for Moodle forms that pair:

- a hidden `tags`
- an empty multiselect `tags[]`

Without that rule, the multiselect companion can clobber the marker that Moodle expects in the
final submission.

### Assignment payload normalization

`CreateCourseAssignmentUseCase` now normalizes these fields before the final submit:

- `coursemodule=0` when blank
- `instance=0` when blank
- `mform_isexpanded_id_submissiontypes=1`
- `mform_isexpanded_id_feedbacktypes=0`
- `mform_isexpanded_id_modstandardgrade=0`
- `cmidnumber=""` when absent
- `lang=""` when absent
- `groupmode=0` when absent
- `tags=_qf__force_multiselect_submission` when absent

It also removes:

- `tags[]`
- `submitbutton`

And forces:

- `submitbutton2=Guardar cambios y regresar al curso`

### Creation success and failure semantics

Assignment/resource creation is considered failed when:

- Moodle redirects to login or otherwise looks unauthenticated
- the HTTP status is `>= 400`
- the final URL still points to `course/modedit.php`

That last case is treated as a validation failure because Moodle typically returns the same form
again when the submission is incomplete or inconsistent.

### Tests

Added tests cover:

- successful assignment creation payload assembly
- date validation failure for invalid non-ISO dates
- extractor handling of hidden `tags` plus empty `tags[]`

Command used locally:

```powershell
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-25.0.2.10-hotspot'
& 'C:\Users\alber\AppData\Local\Programs\IntelliJ IDEA Ultimate\plugins\maven\lib\maven3\bin\mvn.cmd' "-Dtest=CreateCourseAssignmentUseCaseTest,MoodleModEditFormFieldExtractorTest" test
```

### Current limitation

`createPdfResource` is still marked as experimental because it depends on both:

- draft-file upload to Moodle
- final `modedit.php` submission

The shared form-extraction changes improve consistency, but they do not remove the underlying
fragility of the PDF upload flow.
