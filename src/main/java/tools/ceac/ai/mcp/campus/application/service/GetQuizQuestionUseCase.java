package tools.ceac.ai.mcp.campus.application.service;

import tools.ceac.ai.mcp.campus.application.port.out.CampusGateway;
import tools.ceac.ai.mcp.campus.domain.exception.AuthenticationRequiredException;
import tools.ceac.ai.mcp.campus.domain.model.QuizQuestionDetail;
import tools.ceac.ai.mcp.campus.infrastructure.campus.MoodleQuestionDetailParser;
import tools.ceac.ai.mcp.campus.infrastructure.config.CampusProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class GetQuizQuestionUseCase {

    private final CampusGateway campusGateway;
    private final CampusSessionService sessionService;
    private final MoodleQuestionDetailParser parser;
    private final CampusProperties properties;

    public GetQuizQuestionUseCase(CampusGateway campusGateway,
                                   CampusSessionService sessionService,
                                   MoodleQuestionDetailParser parser,
                                   CampusProperties properties) {
        this.campusGateway = campusGateway;
        this.sessionService = sessionService;
        this.parser = parser;
        this.properties = properties;
    }

    public QuizQuestionDetail execute(String questionId, String cmid) {
        if (!sessionService.isAuthenticated()) {
            throw new AuthenticationRequiredException("not_authenticated");
        }
        try {
            String html = campusGateway.getQuestionEditForm(questionId, cmid).body();
            return parser.parse(html, properties.baseUrl(), questionId, cmid);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("question_fetch_failed", e);
        } catch (IOException e) {
            throw new IllegalStateException("question_fetch_failed", e);
        }
    }
}
