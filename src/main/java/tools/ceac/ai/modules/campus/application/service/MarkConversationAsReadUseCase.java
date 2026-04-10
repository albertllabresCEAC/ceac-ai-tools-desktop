package tools.ceac.ai.modules.campus.application.service;

import tools.ceac.ai.modules.campus.application.port.out.CampusGateway;
import tools.ceac.ai.modules.campus.domain.exception.AuthenticationRequiredException;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Marks all messages in a conversation as read via the
 * {@code core_message_mark_all_conversation_messages_as_read} AJAX API.
 */
@Service
public class MarkConversationAsReadUseCase {

    private final CampusGateway campusGateway;
    private final CampusSessionService sessionService;

    public MarkConversationAsReadUseCase(CampusGateway campusGateway, CampusSessionService sessionService) {
        this.campusGateway = campusGateway;
        this.sessionService = sessionService;
    }

    public void execute(String userId, String conversationId) {
        if (!sessionService.isAuthenticated()) {
            throw new AuthenticationRequiredException("not_authenticated");
        }
        String sesskey = sessionService.getSesskey();
        if (sesskey.isBlank()) {
            throw new IllegalStateException("sesskey_not_available");
        }
        try {
            campusGateway.markConversationAsRead(userId, conversationId, sesskey);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("mark_conversation_read_failed", e);
        } catch (IOException e) {
            throw new IllegalStateException("mark_conversation_read_failed", e);
        }
    }
}

