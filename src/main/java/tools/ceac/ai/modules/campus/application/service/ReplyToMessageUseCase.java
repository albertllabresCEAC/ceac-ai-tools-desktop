package tools.ceac.ai.modules.campus.application.service;

import tools.ceac.ai.modules.campus.application.port.out.CampusGateway;
import tools.ceac.ai.modules.campus.domain.exception.AuthenticationRequiredException;
import tools.ceac.ai.modules.campus.infrastructure.campus.MoodleComposeFormParser;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Sends a reply to an existing message via the itop_mailbox plugin.
 * <p>
 * Workflow: GET compose form ({@code /blocks/itop_mailbox/compose.php?message=...&action=reply})
 * â†’ parse hidden fields (sesskey, course, replyto, itemid, attachments, recipients)
 * â†’ POST the reply with the provided subject and content.
 * </p>
 */
@Service
public class ReplyToMessageUseCase {

    private final CampusGateway campusGateway;
    private final CampusSessionService sessionService;
    private final MoodleComposeFormParser parser;

    public ReplyToMessageUseCase(CampusGateway campusGateway,
                                  CampusSessionService sessionService,
                                  MoodleComposeFormParser parser) {
        this.campusGateway = campusGateway;
        this.sessionService = sessionService;
        this.parser = parser;
    }

    public void execute(String studentUserId, String myUserId, String messageTimestamp,
                        String messageId, String subject, String content) {
        if (!sessionService.isAuthenticated()) {
            throw new AuthenticationRequiredException("not_authenticated");
        }
        try {
            String formHtml = campusGateway.getComposeForm(studentUserId, myUserId, messageTimestamp, messageId).body();
            MoodleComposeFormParser.ComposeFormData form = parser.parse(formHtml, "");
            campusGateway.postComposeReply(
                    form.sesskey(), form.course(), form.replyto(),
                    form.itemid(), form.attachments(), form.recipients(),
                    subject, content);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("reply_to_message_failed", e);
        } catch (IOException e) {
            throw new IllegalStateException("reply_to_message_failed", e);
        }
    }
}


