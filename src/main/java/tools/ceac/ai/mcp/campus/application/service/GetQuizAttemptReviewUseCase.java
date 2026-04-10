package tools.ceac.ai.mcp.campus.application.service;

import tools.ceac.ai.mcp.campus.application.port.out.CampusGateway;
import tools.ceac.ai.mcp.campus.domain.exception.AuthenticationRequiredException;
import tools.ceac.ai.mcp.campus.domain.model.QuizAttemptReview;
import tools.ceac.ai.mcp.campus.infrastructure.campus.MoodleQuizAttemptReviewParser;
import tools.ceac.ai.mcp.campus.infrastructure.config.CampusProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Retrieves the full review of a quiz attempt: question text, student answers,
 * answer options, correct answers, and per-question scores.
 */
@Service
public class GetQuizAttemptReviewUseCase {

    private final CampusGateway campusGateway;
    private final CampusSessionService sessionService;
    private final MoodleQuizAttemptReviewParser parser;
    private final CampusProperties properties;

    public GetQuizAttemptReviewUseCase(
            CampusGateway campusGateway,
            CampusSessionService sessionService,
            MoodleQuizAttemptReviewParser parser,
            CampusProperties properties
    ) {
        this.campusGateway = campusGateway;
        this.sessionService = sessionService;
        this.parser = parser;
        this.properties = properties;
    }

    public QuizAttemptReview execute(String attemptId) {
        if (!sessionService.isAuthenticated()) {
            throw new AuthenticationRequiredException("not_authenticated");
        }
        try {
            String html = campusGateway.getQuizAttemptReview(attemptId).body();
            return parser.parse(html, attemptId, properties.baseUrl());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("quiz_attempt_review_fetch_failed", e);
        } catch (IOException e) {
            throw new IllegalStateException("quiz_attempt_review_fetch_failed", e);
        }
    }
}