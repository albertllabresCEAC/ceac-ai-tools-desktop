package tools.ceac.ai.modules.campus.interfaces.api;

import java.util.List;
import tools.ceac.ai.modules.campus.application.service.GetMessageRecipientsUseCase;
import tools.ceac.ai.modules.campus.application.service.SearchMessageRecipientsUseCase;
import tools.ceac.ai.modules.campus.application.service.GetUserProfileUseCase;
import tools.ceac.ai.modules.campus.domain.model.UserProfile;
import tools.ceac.ai.modules.campus.interfaces.api.dto.CourseRefResponse;
import tools.ceac.ai.modules.campus.interfaces.api.dto.MessageRecipientResponse;
import tools.ceac.ai.modules.campus.interfaces.api.dto.UserProfileResponse;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {

    private final GetUserProfileUseCase getUserProfileUseCase;
    private final GetMessageRecipientsUseCase getMessageRecipientsUseCase;
    private final SearchMessageRecipientsUseCase searchMessageRecipientsUseCase;

    public UserController(
            GetUserProfileUseCase getUserProfileUseCase,
            GetMessageRecipientsUseCase getMessageRecipientsUseCase,
            SearchMessageRecipientsUseCase searchMessageRecipientsUseCase) {
        this.getUserProfileUseCase = getUserProfileUseCase;
        this.getMessageRecipientsUseCase = getMessageRecipientsUseCase;
        this.searchMessageRecipientsUseCase = searchMessageRecipientsUseCase;
    }

    @GetMapping("/users")
    public List<MessageRecipientResponse> users() {
        return getMessageRecipientsUseCase.execute()
                .stream()
                .map(r -> new MessageRecipientResponse(r.id(), r.fullName()))
                .toList();
    }

    @GetMapping("/users/search")
    public List<MessageRecipientResponse> searchUsers(
            @Parameter(description = "Texto de busqueda para nombre o id de usuario", example = "Juan")
            @RequestParam String query,
            @Parameter(description = "Maximo de resultados devueltos por Moodle", example = "51")
            @RequestParam(defaultValue = "51") int limitNum,
            @Parameter(description = "Offset para paginacion de resultados", example = "0")
            @RequestParam(defaultValue = "0") int limitFrom) {
        return searchMessageRecipientsUseCase.execute(query, limitNum, limitFrom)
                .stream()
                .map(r -> new MessageRecipientResponse(r.id(), r.fullName()))
                .toList();
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

