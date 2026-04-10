package tools.ceac.ai.mcp.campus.application.service;

import tools.ceac.ai.mcp.campus.application.port.out.CampusGateway;
import tools.ceac.ai.mcp.campus.domain.exception.AuthenticationRequiredException;
import tools.ceac.ai.mcp.campus.domain.model.QuizAttempt;
import tools.ceac.ai.mcp.campus.infrastructure.campus.MoodleQuizReportParser;
import tools.ceac.ai.mcp.campus.infrastructure.config.CampusProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * Retrieves all student attempts and scores for a given quiz module,
 * by POSTing the quiz overview report form and scraping the resulting HTML table.
 */
@Service
public class GetQuizResultsUseCase {

    private final CampusGateway campusGateway;
    private final CampusSessionService sessionService;
    private final MoodleQuizReportParser quizReportParser;
    private final CampusProperties properties;

    public GetQuizResultsUseCase(
            CampusGateway campusGateway,
            CampusSessionService sessionService,
            MoodleQuizReportParser quizReportParser,
            CampusProperties properties
    ) {
        this.campusGateway = campusGateway;
        this.sessionService = sessionService;
        this.quizReportParser = quizReportParser;
        this.properties = properties;
    }

    public List<QuizAttempt> execute(String quizId) {
        if (!sessionService.isAuthenticated()) {
            throw new AuthenticationRequiredException("not_authenticated");
        }
        try {
            String html = campusGateway.getQuizResults(quizId, sessionService.getSesskey()).body();
            return quizReportParser.parse(html, properties.baseUrl());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("quiz_results_fetch_failed", e);
        } catch (IOException e) {
            throw new IllegalStateException("quiz_results_fetch_failed", e);
        }
    }
}