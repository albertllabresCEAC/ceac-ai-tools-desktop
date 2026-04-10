package tools.ceac.ai.mcp.campus.interfaces.api;

import tools.ceac.ai.mcp.campus.application.service.GetAssignSubmissionFilesUseCase;
import tools.ceac.ai.mcp.campus.application.service.GetAssignSubmissionsUseCase;
import tools.ceac.ai.mcp.campus.application.service.GetGradeUseCase;
import tools.ceac.ai.mcp.campus.application.service.SubmitGradeUseCase;
import tools.ceac.ai.mcp.campus.interfaces.api.dto.FileDownloadResponse;
import tools.ceac.ai.mcp.campus.interfaces.api.dto.GradeRequest;
import tools.ceac.ai.mcp.campus.interfaces.api.dto.GradeResponse;
import tools.ceac.ai.mcp.campus.interfaces.api.dto.SubmissionResponse;
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

    public AssignController(
            GetAssignSubmissionsUseCase getAssignSubmissionsUseCase,
            GetAssignSubmissionFilesUseCase getAssignSubmissionFilesUseCase,
            GetGradeUseCase getGradeUseCase,
            SubmitGradeUseCase submitGradeUseCase
    ) {
        this.getAssignSubmissionsUseCase = getAssignSubmissionsUseCase;
        this.getAssignSubmissionFilesUseCase = getAssignSubmissionFilesUseCase;
        this.getGradeUseCase = getGradeUseCase;
        this.submitGradeUseCase = submitGradeUseCase;
    }

    @GetMapping("/assignments/{id}/submissions")
    public List<SubmissionResponse> submissions(
            @Parameter(description = "ID del módulo de tarea Moodle", example = "316309")
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
            @Parameter(description = "ID del módulo de tarea Moodle", example = "316309")
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
            @Parameter(description = "ID del módulo de tarea Moodle", example = "316309")
            @PathVariable String id,
            @Parameter(description = "ID del alumno", example = "3814")
            @PathVariable String userId) {
        var info = getGradeUseCase.execute(id, userId);
        return new GradeResponse(info.grade(), info.feedback());
    }

    @PostMapping("/assignments/{id}/submissions/{userId}/grade")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void grade(
            @Parameter(description = "ID del módulo de tarea Moodle", example = "316309")
            @PathVariable String id,
            @Parameter(description = "ID del alumno", example = "3814")
            @PathVariable String userId,
            @RequestBody GradeRequest request) {
        submitGradeUseCase.execute(id, userId, request.grade(),
                request.feedback(), request.sendNotification());
    }
}
