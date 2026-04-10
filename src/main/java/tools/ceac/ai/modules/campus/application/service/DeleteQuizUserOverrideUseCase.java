package tools.ceac.ai.modules.campus.application.service;

import tools.ceac.ai.modules.campus.application.port.out.CampusGateway;
import tools.ceac.ai.modules.campus.domain.exception.AuthenticationRequiredException;
import tools.ceac.ai.modules.campus.infrastructure.campus.MoodleQuizOverrideEditParser;
import tools.ceac.ai.modules.campus.infrastructure.config.CampusProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Deletes a quiz user override by its override ID.
 * <p>
 * Workflow: GET the delete confirmation form â†’ extract sesskey â†’ POST confirmation.
 * </p>
 */
@Service
public class DeleteQuizUserOverrideUseCase {

    private final CampusGateway campusGateway;
    private final CampusSessionService sessionService;
    private final MoodleQuizOverrideEditParser parser;
    private final CampusProperties properties;

    public DeleteQuizUserOverrideUseCase(
            CampusGateway campusGateway,
            CampusSessionService sessionService,
            MoodleQuizOverrideEditParser parser,
            CampusProperties properties
    ) {
        this.campusGateway = campusGateway;
        this.sessionService = sessionService;
        this.parser = parser;
        this.properties = properties;
    }

    public void execute(String overrideId) {
        if (!sessionService.isAuthenticated()) {
            throw new AuthenticationRequiredException("not_authenticated");
        }
        try {
            String formHtml = campusGateway.getQuizOverrideDeleteForm(overrideId).body();
            String sesskey = parser.parseSesskey(formHtml, properties.baseUrl());
            campusGateway.postQuizOverrideDelete(overrideId, sesskey);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("quiz_override_delete_failed", e);
        } catch (IOException e) {
            throw new IllegalStateException("quiz_override_delete_failed", e);
        }
    }
}

