package tools.ceac.ai.modules.campus.application.service;

import tools.ceac.ai.modules.campus.application.port.out.CampusGateway;
import tools.ceac.ai.modules.campus.domain.exception.AuthenticationRequiredException;
import tools.ceac.ai.modules.campus.domain.model.QuizCommentData;
import tools.ceac.ai.modules.campus.infrastructure.campus.MoodleQuizCommentParser;
import tools.ceac.ai.modules.campus.infrastructure.config.CampusProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Saves a mark and/or comment for an open-ended question in a quiz attempt.
 * <p>
 * Workflow: GET comment form â†’ parse hidden fields (sesskey, usageId, sequencecheck, etc.)
 * â†’ POST to {@code /mod/quiz/comment.php} with the provided mark and comment.
 * </p>
 */
@Service
public class SaveQuizCommentUseCase {

    private final CampusGateway campusGateway;
    private final CampusSessionService sessionService;
    private final MoodleQuizCommentParser parser;
    private final CampusProperties properties;

    public SaveQuizCommentUseCase(
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

    public void execute(String attemptId, String slot, String mark, String comentario) {
        if (!sessionService.isAuthenticated()) {
            throw new AuthenticationRequiredException("not_authenticated");
        }
        try {
            String html = campusGateway.getQuizComment(attemptId, slot).body();
            QuizCommentData data = parser.parse(html, attemptId, slot, properties.baseUrl());

            campusGateway.postQuizComment(
                    attemptId, slot, data.usageId(),
                    data.sesskey(), data.sequencecheck(),
                    data.itemid(), data.commentFormat(),
                    mark, data.maxMark(),
                    data.minFraction(), data.maxFraction(),
                    comentario
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("quiz_comment_save_failed", e);
        } catch (IOException e) {
            throw new IllegalStateException("quiz_comment_save_failed", e);
        }
    }
}

