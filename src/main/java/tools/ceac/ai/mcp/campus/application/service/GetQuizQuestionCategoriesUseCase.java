package tools.ceac.ai.mcp.campus.application.service;

import tools.ceac.ai.mcp.campus.application.port.out.CampusGateway;
import tools.ceac.ai.mcp.campus.domain.exception.AuthenticationRequiredException;
import tools.ceac.ai.mcp.campus.domain.model.QuizQuestionCategory;
import tools.ceac.ai.mcp.campus.infrastructure.campus.MoodleQuestionBankParser;
import tools.ceac.ai.mcp.campus.infrastructure.config.CampusProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class GetQuizQuestionCategoriesUseCase {

    private final CampusGateway campusGateway;
    private final CampusSessionService sessionService;
    private final MoodleQuestionBankParser parser;
    private final CampusProperties properties;

    public GetQuizQuestionCategoriesUseCase(CampusGateway campusGateway,
                                             CampusSessionService sessionService,
                                             MoodleQuestionBankParser parser,
                                             CampusProperties properties) {
        this.campusGateway = campusGateway;
        this.sessionService = sessionService;
        this.parser = parser;
        this.properties = properties;
    }

    public List<QuizQuestionCategory> execute(String cmid) {
        if (!sessionService.isAuthenticated()) {
            throw new AuthenticationRequiredException("not_authenticated");
        }
        try {
            String html = campusGateway.getQuestionBank(cmid).body();
            return parser.parse(html, properties.baseUrl()).categories();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("question_bank_fetch_failed", e);
        } catch (IOException e) {
            throw new IllegalStateException("question_bank_fetch_failed", e);
        }
    }
}
