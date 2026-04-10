package tools.ceac.ai.modules.campus.application.service;

import tools.ceac.ai.modules.campus.application.port.out.CampusGateway;
import tools.ceac.ai.modules.campus.domain.exception.AuthenticationRequiredException;
import tools.ceac.ai.modules.campus.domain.model.QuizCommentData;
import tools.ceac.ai.modules.campus.infrastructure.campus.MoodleQuizCommentParser;
import tools.ceac.ai.modules.campus.infrastructure.config.CampusProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Retrieves the comment/grading data for a specific open-ended question slot
 * in a quiz attempt (question text, current mark, max mark, sesskey, etc.).
 * This data is needed both to display the question and to build the POST request
 * for {@code SaveQuizCommentUseCase}.
 */
@Service
public class GetQuizCommentUseCase {

    private final CampusGateway campusGateway;
    private final CampusSessionService sessionService;
    private final MoodleQuizCommentParser parser;
    private final CampusProperties properties;

    public GetQuizCommentUseCase(
            CampusGateway campusGateway,
            CampusSessionService sessionService,
            MoodleQuizCommentParser parser,
            CampusProperties properties
    ) {
        this.campusGateway = campusGateway;
        this.sessionService = sessionService;
        this.parser = parser;
        this.properties = properties;
    }

    public QuizCommentData execute(String attemptId, String slot) {
        if (!sessionService.isAuthenticated()) {
            throw new AuthenticationRequiredException("not_authenticated");
        }
        try {
            String html = campusGateway.getQuizComment(attemptId, slot).body();
            return parser.parse(html, attemptId, slot, properties.baseUrl());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("quiz_comment_fetch_failed", e);
        } catch (IOException e) {
            throw new IllegalStateException("quiz_comment_fetch_failed", e);
        }
    }
}

