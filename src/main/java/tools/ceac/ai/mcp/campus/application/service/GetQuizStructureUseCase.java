package tools.ceac.ai.mcp.campus.application.service;

import tools.ceac.ai.mcp.campus.application.port.out.CampusGateway;
import tools.ceac.ai.mcp.campus.domain.exception.AuthenticationRequiredException;
import tools.ceac.ai.mcp.campus.domain.model.QuizStructure;
import tools.ceac.ai.mcp.campus.infrastructure.campus.MoodleQuizEditParser;
import tools.ceac.ai.mcp.campus.infrastructure.config.CampusProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class GetQuizStructureUseCase {

    private final CampusGateway campusGateway;
    private final CampusSessionService sessionService;
    private final MoodleQuizEditParser quizEditParser;
    private final CampusProperties properties;

    public GetQuizStructureUseCase(CampusGateway campusGateway,
                                   CampusSessionService sessionService,
                                   MoodleQuizEditParser quizEditParser,
                                   CampusProperties properties) {
        this.campusGateway = campusGateway;
        this.sessionService = sessionService;
        this.quizEditParser = quizEditParser;
        this.properties = properties;
    }

    public QuizStructure execute(String cmid) {
        if (!sessionService.isAuthenticated()) {
            throw new AuthenticationRequiredException("not_authenticated");
        }
        try {
            String html = campusGateway.getQuizEdit(cmid).body();
            return quizEditParser.parse(html, cmid, properties.baseUrl());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("quiz_edit_fetch_failed", e);
        } catch (IOException e) {
            throw new IllegalStateException("quiz_edit_fetch_failed", e);
        }
    }
}