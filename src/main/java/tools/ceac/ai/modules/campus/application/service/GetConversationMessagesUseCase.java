package tools.ceac.ai.modules.campus.application.service;

import tools.ceac.ai.modules.campus.application.port.out.CampusGateway;
import tools.ceac.ai.modules.campus.domain.exception.AuthenticationRequiredException;
import tools.ceac.ai.modules.campus.domain.model.ConversationDetail;
import tools.ceac.ai.modules.campus.infrastructure.campus.MoodleConversationMessagesParser;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Retrieves the messages of a specific conversation via the
 * {@code core_message_get_conversation_messages} AJAX API.
 */
@Service
public class GetConversationMessagesUseCase {

    private final CampusGateway campusGateway;
    private final CampusSessionService sessionService;
    private final MoodleConversationMessagesParser parser;

    public GetConversationMessagesUseCase(CampusGateway campusGateway,
                                          CampusSessionService sessionService,
                                          MoodleConversationMessagesParser parser) {
        this.campusGateway = campusGateway;
        this.sessionService = sessionService;
        this.parser = parser;
    }

    public ConversationDetail execute(String currentUserId, String convId,
                                      boolean newest, int limitNum, int limitFrom) {
        if (!sessionService.isAuthenticated()) {
            throw new AuthenticationRequiredException("not_authenticated");
        }
        String sesskey = sessionService.getSesskey();
        if (sesskey.isBlank()) {
            throw new IllegalStateException("sesskey_not_available");
        }
        try {
            String json = campusGateway.getConversationMessages(currentUserId, convId, sesskey, newest, limitNum, limitFrom).body();
            return parser.parse(json);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("conversation_messages_fetch_failed", e);
        } catch (IOException e) {
            throw new IllegalStateException("conversation_messages_fetch_failed", e);
        }
    }
}

