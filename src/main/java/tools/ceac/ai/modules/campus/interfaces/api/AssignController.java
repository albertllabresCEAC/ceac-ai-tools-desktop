package tools.ceac.ai.modules.campus.interfaces.api;

import io.swagger.v3.oas.annotations.Operation;
import tools.ceac.ai.modules.campus.application.service.GetAssignSubmissionFilesUseCase;
import tools.ceac.ai.modules.campus.application.service.GetAssignSubmissionsUseCase;
import tools.ceac.ai.modules.campus.application.service.GetGradeUseCase;
import tools.ceac.ai.modules.campus.application.service.SubmitGradeUseCase;
import tools.ceac.ai.modules.campus.application.service.CreateCourseAssignmentUseCase;
import tools.ceac.ai.modules.campus.interfaces.api.dto.CreateCourseAssignmentRequest;
import tools.ceac.ai.modules.campus.interfaces.api.dto.CreateCourseAssignmentResponse;
import tools.ceac.ai.modules.campus.interfaces.api.dto.FileDownloadResponse;
import tools.ceac.ai.modules.campus.interfaces.api.dto.GradeRequest;
import tools.ceac.ai.modules.campus.interfaces.api.dto.GradeResponse;
import tools.ceac.ai.modules.campus.interfaces.api.dto.SubmissionResponse;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AssignController {

    private final GetAssignSubmissionsUseCase getAssignSubmissionsUseCase;
    private final GetAssignSubmissionFilesUseCase getAssignSubmissionFilesUseCase;
    private final GetGradeUseCase getGradeUseCase;
    private final SubmitGradeUseCase submitGradeUseCase;
    private final CreateCourseAssignmentUseCase createCourseAssignmentUseCase;

    public AssignController(
            GetAssignSubmissionsUseCase getAssignSubmissionsUseCase,
            GetAssignSubmissionFilesUseCase getAssignSubmissionFilesUseCase,
            GetGradeUseCase getGradeUseCase,
            SubmitGradeUseCase submitGradeUseCase,
            CreateCourseAssignmentUseCase createCourseAssignmentUseCase
    ) {
        this.getAssignSubmissionsUseCase = getAssignSubmissionsUseCase;
        this.getAssignSubmissionFilesUseCase = getAssignSubmissionFilesUseCase;
        this.getGradeUseCase = getGradeUseCase;
        this.submitGradeUseCase = submitGradeUseCase;
        this.createCourseAssignmentUseCase = createCourseAssignmentUseCase;
    }

    @PostMapping("/courses/{courseId}/assignments")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crea una tarea en una seccion del curso sin adjuntar documentos")
    public CreateCourseAssignmentResponse createAssignment(
            @Parameter(name = "courseId", in = ParameterIn.PATH, required = true,
                    description = "ID del curso Moodle", example = "8202")
            @PathVariable String courseId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
                    schema = @Schema(implementation = CreateCourseAssignmentRequest.class),
                    examples = @ExampleObject(value = """
                            {
                              "section": 3,
                              "name": "Actividad final del modulo",
                              "description": "<p>Lee las instrucciones y entrega la actividad final.</p>",
                              "activityInstructions": "<p>Entrega un PDF con el desarrollo del caso practico.</p>",
                              "availableFrom": "2026-04-20T08:00",
                              "dueAt": "2026-04-30T23:59",
                              "cutoffAt": "2026-05-02T23:59",
                              "gradingDueAt": "2026-05-07T23:59",
                              "visible": true,
                              "showDescription": false,
                              "alwaysShowDescription": false,
                              "sendNotifications": true,
                              "sendLateNotifications": false,
                              "sendStudentNotifications": true
                            }""")))
            @RequestBody CreateCourseAssignmentRequest request) {
        return createCourseAssignmentUseCase.execute(courseId, request);
    }

    @GetMapping("/assignments/{id}/submissions")
    public List<SubmissionResponse> submissions(
            @Parameter(description = "ID del mÃ³dulo de tarea Moodle", example = "316309")
            @PathVariable String id) {
        return getAssignSubmissionsUseCase.execute(id)
                .stream()
                .map(s -> new SubmissionResponse(
                        s.userId(), s.fullName(), s.email(),
                        s.status(), s.submittedAt(), s.files()))
                .toList();
    }

    @GetMapping("/assignments/{id}/submissions/{userId}/files")
    public List<FileDownloadResponse> submissionFiles(
            @Parameter(description = "ID del mÃ³dulo de tarea Moodle", example = "316309")
            @PathVariable String id,
            @Parameter(description = "ID del alumno", example = "3814")
            @PathVariable String userId) {
        return getAssignSubmissionFilesUseCase.execute(id, userId)
                .stream()
                .map(f -> new FileDownloadResponse(f.filename(), f.mimeType(), f.content()))
                .toList();
    }

    @GetMapping("/assignments/{id}/submissions/{userId}/grade")
    public GradeResponse getGrade(
            @Parameter(description = "ID del mÃ³dulo de tarea Moodle", example = "316309")
            @PathVariable String id,
            @Parameter(description = "ID del alumno", example = "3814")
            @PathVariable String userId) {
        var info = getGradeUseCase.execute(id, userId);
        return new GradeResponse(info.grade(), info.feedback());
    }

    @PostMapping("/assignments/{id}/submissions/{userId}/grade")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void grade(
            @Parameter(description = "ID del mÃ³dulo de tarea Moodle", example = "316309")
            @PathVariable String id,
            @Parameter(description = "ID del alumno", example = "3814")
            @PathVariable String userId,
            @RequestBody GradeRequest request) {
        submitGradeUseCase.execute(id, userId, request.grade(),
                request.feedback(), request.sendNotification());
    }
}


