package tools.ceac.ai.modules.campus.interfaces.api;

import tools.ceac.ai.modules.campus.application.service.GetDashboardUseCase;
import tools.ceac.ai.modules.campus.interfaces.api.dto.ProfileResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ProfileController {
    private final GetDashboardUseCase getDashboardUseCase;

    public ProfileController(GetDashboardUseCase getDashboardUseCase) {
        this.getDashboardUseCase = getDashboardUseCase;
    }

    @GetMapping("/profile")
    public ProfileResponse profile() {
        var snapshot = getDashboardUseCase.execute();
        return new ProfileResponse(
                snapshot.userDisplayName(),
                snapshot.userId(),
                snapshot.email(),
                snapshot.language(),
                snapshot.unreadMessages()
        );
    }
}


