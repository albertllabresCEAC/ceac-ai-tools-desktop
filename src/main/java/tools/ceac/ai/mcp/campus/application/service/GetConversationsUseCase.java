package tools.ceac.ai.mcp.campus.application.service;

import tools.ceac.ai.mcp.campus.application.port.out.CampusGateway;
import tools.ceac.ai.mcp.campus.domain.exception.AuthenticationRequiredException;
import tools.ceac.ai.mcp.campus.domain.model.Conversation;
import tools.ceac.ai.mcp.campus.infrastructure.campus.MoodleConversationsParser;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * Lists the conversations of a Moodle user via the
 * {@code core_message_get_conversations} AJAX API.
 */
@Service
public class GetConversationsUseCase {

    private final CampusGateway campusGateway;
    private final CampusSessionService sessionService;
    private final MoodleConversationsParser parser;

    public GetConversationsUseCase(CampusGateway campusGateway,
                                   CampusSessionService sessionService,
                                   MoodleConversationsParser parser) {
        this.campusGateway = campusGateway;
        this.sessionService = sessionService;
        this.parser = parser;
    }

    public List<Conversation> execute(String userId, int type, int limitNum, int limitFrom,
                                      boolean favourites, boolean mergeSelf) {
        if (!sessionService.isAuthenticated()) {
            throw new AuthenticationRequiredException("not_authenticated");
        }
        String sesskey = sessionService.getSesskey();
        if (sesskey.isBlank()) {
            throw new IllegalStateException("sesskey_not_available");
        }
        try {
            String json = campusGateway.getConversations(userId, sesskey, type, limitNum, limitFrom, favourites, mergeSelf).body();
            return parser.parse(json);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("conversations_fetch_failed", e);
        } catch (IOException e) {
            throw new IllegalStateException("conversations_fetch_failed", e);
        }
    }
}