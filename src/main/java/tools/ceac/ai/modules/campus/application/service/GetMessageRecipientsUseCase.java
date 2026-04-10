package tools.ceac.ai.modules.campus.application.service;

import tools.ceac.ai.modules.campus.application.port.out.CampusGateway;
import tools.ceac.ai.modules.campus.domain.exception.AuthenticationRequiredException;
import tools.ceac.ai.modules.campus.domain.model.MessageRecipient;
import tools.ceac.ai.modules.campus.infrastructure.campus.MoodleComposeFormParser;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * Retrieves all potential message recipients (students across all courses)
 * by POSTing to the itop_mailbox compose form with {@code allcourses=1}
 * and parsing the {@code <optgroup label="Alumnos">} section.
 */
@Service
public class GetMessageRecipientsUseCase {

    private final CampusGateway campusGateway;
    private final CampusSessionService sessionService;
    private final MoodleComposeFormParser parser;

    public GetMessageRecipientsUseCase(CampusGateway campusGateway,
                                        CampusSessionService sessionService,
                                        MoodleComposeFormParser parser) {
        this.campusGateway = campusGateway;
        this.sessionService = sessionService;
        this.parser = parser;
    }

    public List<MessageRecipient> execute() {
        if (!sessionService.isAuthenticated()) {
            throw new AuthenticationRequiredException("not_authenticated");
        }
        try {
            String formHtml = campusGateway.getComposeFormBlank().body();
            String sesskey = parser.parse(formHtml, "").sesskey();
            String allCoursesHtml = campusGateway.postComposeAllCourses(sesskey).body();
            return parser.parseRecipients(allCoursesHtml);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("get_message_recipients_failed", e);
        } catch (IOException e) {
            throw new IllegalStateException("get_message_recipients_failed", e);
        }
    }
}

