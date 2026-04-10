package tools.ceac.ai.modules.campus.application.service;

import tools.ceac.ai.modules.campus.application.port.out.CampusGateway;
import tools.ceac.ai.modules.campus.domain.exception.AuthenticationRequiredException;
import tools.ceac.ai.modules.campus.domain.model.GraderInfo;
import tools.ceac.ai.modules.campus.infrastructure.campus.MoodleGraderPageParser;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class GetGraderInfoUseCase {

    private final CampusGateway campusGateway;
    private final CampusSessionService sessionService;
    private final MoodleGraderPageParser graderPageParser;

    public GetGraderInfoUseCase(
            CampusGateway campusGateway,
            CampusSessionService sessionService,
            MoodleGraderPageParser graderPageParser
    ) {
        this.campusGateway = campusGateway;
        this.sessionService = sessionService;
        this.graderPageParser = graderPageParser;
    }

    public GraderInfo execute(String moduleId, String studentUserId) {
        if (!sessionService.isAuthenticated()) {
            throw new AuthenticationRequiredException("not_authenticated");
        }
        try {
            String html = campusGateway.getGraderPage(moduleId, studentUserId).body();
            return graderPageParser.parse(html);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("grader_fetch_failed", e);
        } catch (IOException e) {
            throw new IllegalStateException("grader_fetch_failed", e);
        }
    }
}


