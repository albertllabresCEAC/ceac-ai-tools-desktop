package tools.ceac.ai.modules.campus.interfaces.api;

import tools.ceac.ai.modules.campus.application.service.CampusSessionService;
import tools.ceac.ai.modules.campus.interfaces.api.dto.HealthResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {
    private final CampusSessionService sessionService;

    public HealthController(CampusSessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("/health")
    public HealthResponse health() {
        return new HealthResponse("ok", sessionService.isAuthenticated());
    }
}


