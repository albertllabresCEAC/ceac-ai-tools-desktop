package tools.ceac.ai.mcp.campus.application.service;

import tools.ceac.ai.mcp.campus.application.port.out.CampusGateway;
import tools.ceac.ai.mcp.campus.domain.exception.AuthenticationRequiredException;
import tools.ceac.ai.mcp.campus.infrastructure.campus.MoodleComposeFormParser;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * Sends a new (non-reply) message via the itop_mailbox plugin.
 * <p>
 * Workflow: GET blank compose form ({@code /blocks/itop_mailbox/compose.php})
 * → parse hidden fields (sesskey, course, itemid, attachments)
 * → POST the message with the provided recipients, subject and content.
 * </p>
 */
@Service
public class SendNewMessageUseCase {

    private final CampusGateway campusGateway;
    private final CampusSessionService sessionService;
    private final MoodleComposeFormParser parser;

    public SendNewMessageUseCase(CampusGateway campusGateway,
                                  CampusSessionService sessionService,
                                  MoodleComposeFormParser parser) {
        this.campusGateway = campusGateway;
        this.sessionService = sessionService;
        this.parser = parser;
    }

    public void execute(List<String> recipientIds, String subject, String content) {
        if (!sessionService.isAuthenticated()) {
            throw new AuthenticationRequiredException("not_authenticated");
        }
        try {
            String blankHtml = campusGateway.getComposeFormBlank().body();
            String sesskey = parser.parse(blankHtml, "").sesskey();
            String composeHtml = campusGateway.postComposeAllCourses(sesskey).body();
            MoodleComposeFormParser.ComposeFormData form = parser.parse(composeHtml, "");
            campusGateway.postSendMessage(
                    form.sesskey(), form.course(),
                    recipientIds,
                    form.itemid(), form.attachments(),
                    subject, content);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("send_new_message_failed", e);
        } catch (IOException e) {
            throw new IllegalStateException("send_new_message_failed", e);
        }
    }
}