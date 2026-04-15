package tools.ceac.ai.modules.campus.application.service;

import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import tools.ceac.ai.modules.campus.application.port.out.CampusGateway;
import tools.ceac.ai.modules.campus.domain.exception.AuthenticationRequiredException;
import tools.ceac.ai.modules.campus.domain.model.ActivitySummary;
import tools.ceac.ai.modules.campus.domain.model.CourseDetail;
import tools.ceac.ai.modules.campus.domain.model.SectionSummary;
import tools.ceac.ai.modules.campus.infrastructure.campus.MoodleModEditFormFieldExtractor;
import tools.ceac.ai.modules.campus.infrastructure.config.CampusProperties;
import tools.ceac.ai.modules.campus.interfaces.api.dto.CreateCourseAssignmentRequest;
import tools.ceac.ai.modules.campus.interfaces.api.dto.CreateCourseAssignmentResponse;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CreateCourseAssignmentUseCase {

    private static final String DEFAULT_AVAILABILITY_JSON = "{\"op\":\"&\",\"c\":[],\"showc\":[]}";
    private static final String ASSIGN_MODULE = "assign";
    private static final String FORCE_MULTISELECT_SUBMISSION = "_qf__force_multiselect_submission";
    private static final String SAVE_AND_RETURN_TO_COURSE = "Guardar cambios y regresar al curso";

    private final CampusGateway campusGateway;
    private final CampusSessionService sessionService;
    private final MoodleModEditFormFieldExtractor formFieldExtractor;
    private final CampusProperties properties;
    private final GetCourseUseCase getCourseUseCase;

    public CreateCourseAssignmentUseCase(CampusGateway campusGateway,
                                         CampusSessionService sessionService,
                                         MoodleModEditFormFieldExtractor formFieldExtractor,
                                         CampusProperties properties,
                                         GetCourseUseCase getCourseUseCase) {
        this.campusGateway = campusGateway;
        this.sessionService = sessionService;
        this.formFieldExtractor = formFieldExtractor;
        this.properties = properties;
        this.getCourseUseCase = getCourseUseCase;
    }

    public CreateCourseAssignmentResponse execute(String courseId, CreateCourseAssignmentRequest request) {
        if (!sessionService.isAuthenticated()) {
            throw new AuthenticationRequiredException("not_authenticated");
        }
        validateRequest(courseId, request);

        try {
            HttpResponse<String> formResponse = campusGateway.getCourseModEditForm(
                    courseId,
                    request.section().toString(),
                    ASSIGN_MODULE
            );
            ensureAuthenticated(formResponse, "assign_form_auth_failed");

            Element form = formFieldExtractor.requireModEditForm(
                    formResponse.body(),
                    properties.baseUrl(),
                    "assign_modedit_form_not_found"
            );
            Map<String, String> formFields = formFieldExtractor.extractFormFields(form);

            sessionService.storeSesskey(firstNonBlank(
                    formFields.get("sesskey"),
                    sessionService.getSesskey()
            ));
            if (sessionService.getSesskey().isBlank()) {
                sessionService.storeSesskeyFromHtml(formResponse.body());
            }

            Map<String, String> params = buildAssignmentParams(courseId, request, formFields);
            HttpResponse<String> createResponse = campusGateway.postCourseModEdit(params);
            ensureAuthenticated(createResponse, "assign_create_auth_failed");
            validateCreateResponse(createResponse);

            ActivitySummary createdAssignment = resolveCreatedAssignment(courseId, request.section(), request.name());
            String assignmentUrl = createdAssignment != null && notBlank(createdAssignment.url())
                    ? createdAssignment.url()
                    : extractAssignmentUrl(createResponse);
            String courseUrl = properties.baseUrl() + "/course/view.php?id=" + courseId;

            return new CreateCourseAssignmentResponse(
                    courseId,
                    request.section(),
                    request.name(),
                    createdAssignment != null ? createdAssignment.id() : extractModuleId(assignmentUrl),
                    assignmentUrl,
                    courseUrl
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("assign_create_failed", exception);
        } catch (IOException exception) {
            throw new IllegalStateException("assign_create_failed: " + exception.getMessage(), exception);
        }
    }

    private void validateRequest(String courseId, CreateCourseAssignmentRequest request) {
        if (!notBlank(courseId)) {
            throw new IllegalArgumentException("course_id_required");
        }
        if (request == null) {
            throw new IllegalArgumentException("request_required");
        }
        if (request.section() == null) {
            throw new IllegalArgumentException("section_required");
        }
        if (!notBlank(request.name())) {
            throw new IllegalArgumentException("assignment_name_required");
        }
    }

    private void ensureAuthenticated(HttpResponse<String> response, String failureCode) {
        if (!campusGateway.looksAuthenticated(response)) {
            throw new AuthenticationRequiredException(failureCode);
        }
    }

    private void validateCreateResponse(HttpResponse<String> response) {
        if (response.statusCode() >= 400) {
            throw new IllegalStateException("assign_create_failed");
        }
        String finalUrl = response.uri() != null ? response.uri().toString() : "";
        if (finalUrl.contains("/course/modedit.php")) {
            throw new IllegalStateException("assign_create_validation_failed");
        }
    }

    private Map<String, String> buildAssignmentParams(String courseId,
                                                      CreateCourseAssignmentRequest request,
                                                      Map<String, String> formFields) {
        Map<String, String> params = new LinkedHashMap<>(formFields);
        params.put("completionunlocked", params.getOrDefault("completionunlocked", "1"));
        params.put("course", courseId);
        params.put("coursemodule", defaultIfBlank(params.get("coursemodule"), "0"));
        params.put("section", request.section().toString());
        params.put("module", params.getOrDefault("module", "1"));
        params.put("modulename", params.getOrDefault("modulename", ASSIGN_MODULE));
        params.put("instance", defaultIfBlank(params.get("instance"), "0"));
        params.put("add", params.getOrDefault("add", ASSIGN_MODULE));
        params.put("update", params.getOrDefault("update", "0"));
        params.put("return", params.getOrDefault("return", "0"));
        params.put("sr", params.getOrDefault("sr", "0"));
        params.put("beforemod", params.getOrDefault("beforemod", "0"));
        params.put("showonly", params.getOrDefault("showonly", ""));
        params.put("_qf__mod_assign_mod_form", params.getOrDefault("_qf__mod_assign_mod_form", "1"));
        params.put("mform_showmore_id_optionssection", params.getOrDefault("mform_showmore_id_optionssection", "0"));
        params.put("mform_isexpanded_id_general", params.getOrDefault("mform_isexpanded_id_general", "1"));
        params.put("mform_isexpanded_id_availability", params.getOrDefault("mform_isexpanded_id_availability", "1"));
        params.put("mform_isexpanded_id_submissiontypes", params.getOrDefault("mform_isexpanded_id_submissiontypes", "1"));
        params.put("mform_isexpanded_id_feedbacktypes", params.getOrDefault("mform_isexpanded_id_feedbacktypes", "0"));
        params.put("mform_isexpanded_id_submissionsettings", params.getOrDefault("mform_isexpanded_id_submissionsettings", "0"));
        params.put("mform_isexpanded_id_groupsubmissionsettings", params.getOrDefault("mform_isexpanded_id_groupsubmissionsettings", "0"));
        params.put("mform_isexpanded_id_notifications", params.getOrDefault("mform_isexpanded_id_notifications", "0"));
        params.put("mform_isexpanded_id_modstandardgrade", params.getOrDefault("mform_isexpanded_id_modstandardgrade", "0"));
        params.put("mform_isexpanded_id_modstandardelshdr", params.getOrDefault("mform_isexpanded_id_modstandardelshdr", "0"));
        params.put("mform_isexpanded_id_availabilityconditionsheader", params.getOrDefault("mform_isexpanded_id_availabilityconditionsheader", "0"));
        params.put("mform_isexpanded_id_activitycompletionheader", params.getOrDefault("mform_isexpanded_id_activitycompletionheader", "0"));
        params.put("mform_isexpanded_id_tagshdr", params.getOrDefault("mform_isexpanded_id_tagshdr", "0"));
        params.put("name", request.name().trim());
        params.put("introeditor[text]", defaultString(request.description()));
        params.put("introeditor[format]", params.getOrDefault("introeditor[format]", "1"));
        params.put("introeditor[itemid]", params.getOrDefault("introeditor[itemid]", ""));
        params.put("activityeditor[text]", firstNonBlank(
                normalizeBlank(request.activityInstructions()),
                params.get("activityeditor[text]"),
                ""
        ));
        params.put("activityeditor[format]", params.getOrDefault("activityeditor[format]", "1"));
        params.put("activityeditor[itemid]", params.getOrDefault("activityeditor[itemid]", ""));
        params.put("showdescription", booleanToNumeric(request.showDescription(), params.get("showdescription"), "0"));
        params.put("alwaysshowdescription", booleanToNumeric(request.alwaysShowDescription(), params.get("alwaysshowdescription"), "0"));
        params.put("sendnotifications", booleanToNumeric(request.sendNotifications(), params.get("sendnotifications"), "0"));
        params.put("sendlatenotifications", booleanToNumeric(request.sendLateNotifications(), params.get("sendlatenotifications"), "0"));
        params.put("sendstudentnotifications", booleanToNumeric(request.sendStudentNotifications(), params.get("sendstudentnotifications"), "1"));
        params.put("visible", booleanToNumeric(request.visible(), params.get("visible"), "1"));
        params.put("cmidnumber", defaultIfBlank(params.get("cmidnumber"), ""));
        params.put("lang", defaultIfBlank(params.get("lang"), ""));
        params.put("groupmode", defaultIfBlank(params.get("groupmode"), "0"));
        params.put("availabilityconditionsjson", params.getOrDefault("availabilityconditionsjson", DEFAULT_AVAILABILITY_JSON));
        params.put("completion", params.getOrDefault("completion", "0"));
        params.put("tags", defaultIfBlank(params.get("tags"), FORCE_MULTISELECT_SUBMISSION));

        applyOptionalDateTime(params, "allowsubmissionsfromdate", request.availableFrom());
        applyOptionalDateTime(params, "duedate", request.dueAt());
        applyOptionalDateTime(params, "cutoffdate", request.cutoffAt());
        applyOptionalDateTime(params, "gradingduedate", request.gradingDueAt());

        params.remove("tags[]");
        params.remove("submitbutton");
        params.put("submitbutton2", SAVE_AND_RETURN_TO_COURSE);
        return params;
    }

    private void applyOptionalDateTime(Map<String, String> params, String prefix, String rawValue) {
        if (!notBlank(rawValue)) {
            return;
        }
        ZonedDateTime dateTime = parseDateTime(rawValue, prefix);
        params.put(prefix + "[enabled]", "1");
        params.put(prefix + "[day]", Integer.toString(dateTime.getDayOfMonth()));
        params.put(prefix + "[month]", Integer.toString(dateTime.getMonthValue()));
        params.put(prefix + "[year]", Integer.toString(dateTime.getYear()));
        params.put(prefix + "[hour]", Integer.toString(dateTime.getHour()));
        params.put(prefix + "[minute]", Integer.toString(dateTime.getMinute()));
    }

    private ZonedDateTime parseDateTime(String rawValue, String fieldName) {
        String normalized = rawValue.trim();
        try {
            return ZonedDateTime.parse(normalized).withZoneSameInstant(ZoneId.systemDefault());
        } catch (Exception ignored) {
        }
        try {
            return OffsetDateTime.parse(normalized).atZoneSameInstant(ZoneId.systemDefault());
        } catch (Exception ignored) {
        }
        try {
            return LocalDateTime.parse(normalized).atZone(ZoneId.systemDefault());
        } catch (Exception ignored) {
        }
        try {
            return LocalDate.parse(normalized).atStartOfDay(ZoneId.systemDefault());
        } catch (Exception ignored) {
        }
        throw new IllegalArgumentException(fieldName + "_invalid_datetime");
    }

    private ActivitySummary resolveCreatedAssignment(String courseId, Integer sectionNumber, String assignmentName) {
        CourseDetail course = getCourseUseCase.execute(courseId);
        return findAssignment(course.sections(), sectionNumber, assignmentName == null ? "" : assignmentName.trim());
    }

    private ActivitySummary findAssignment(List<SectionSummary> sections, Integer sectionNumber, String assignmentName) {
        for (SectionSummary section : sections) {
            if (section.number() == sectionNumber.intValue()) {
                ActivitySummary exactMatch = section.activities().stream()
                        .filter(activity -> ASSIGN_MODULE.equals(activity.type()))
                        .filter(activity -> assignmentName.equals(activity.name()))
                        .reduce((first, second) -> second)
                        .orElse(null);
                if (exactMatch != null) {
                    return exactMatch;
                }
            }
            ActivitySummary nested = findAssignment(section.children(), sectionNumber, assignmentName);
            if (nested != null) {
                return nested;
            }
        }
        return null;
    }

    private String extractAssignmentUrl(HttpResponse<String> response) {
        if (response == null || response.uri() == null) {
            return null;
        }
        String finalUrl = response.uri().toString();
        return finalUrl.contains("/mod/assign/view.php?id=") ? finalUrl : null;
    }

    private String extractModuleId(String assignmentUrl) {
        if (!notBlank(assignmentUrl)) {
            return null;
        }
        int idMarker = assignmentUrl.indexOf("id=");
        if (idMarker < 0) {
            return null;
        }
        String value = assignmentUrl.substring(idMarker + 3);
        int nextAmpersand = value.indexOf('&');
        return nextAmpersand >= 0 ? value.substring(0, nextAmpersand) : value;
    }

    private String booleanToNumeric(Boolean value, String fallback, String defaultValue) {
        if (value != null) {
            return value ? "1" : "0";
        }
        if (notBlank(fallback)) {
            return fallback;
        }
        return defaultValue;
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return notBlank(value) ? value : defaultValue;
    }

    private String normalizeBlank(String value) {
        return notBlank(value) ? value : null;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (notBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}
