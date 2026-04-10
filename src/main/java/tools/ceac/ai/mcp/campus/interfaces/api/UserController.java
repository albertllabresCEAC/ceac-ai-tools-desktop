package tools.ceac.ai.mcp.campus.interfaces.api;

import tools.ceac.ai.mcp.campus.application.service.GetUserProfileUseCase;
import tools.ceac.ai.mcp.campus.domain.model.UserProfile;
import tools.ceac.ai.mcp.campus.interfaces.api.dto.CourseRefResponse;
import tools.ceac.ai.mcp.campus.interfaces.api.dto.UserProfileResponse;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {

    private final GetUserProfileUseCase getUserProfileUseCase;

    public UserController(GetUserProfileUseCase getUserProfileUseCase) {
        this.getUserProfileUseCase = getUserProfileUseCase;
    }

    @GetMapping("/users/{id}/profile")
    public UserProfileResponse profile(
            @Parameter(description = "ID del usuario Moodle", example = "11681")
            @PathVariable String id) {
        UserProfile p = getUserProfileUseCase.execute(id);
        return new UserProfileResponse(
                p.userId(),
                p.fullName(),
                p.email(),
                p.country(),
                p.timezone(),
                p.courses().stream()
                        .map(c -> new CourseRefResponse(c.courseId(), c.courseName()))
                        .toList(),
                p.firstAccess(),
                p.lastAccess()
        );
    }
}