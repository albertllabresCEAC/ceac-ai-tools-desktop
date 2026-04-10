package tools.ceac.ai.modules.campus.domain.model;

import java.util.List;

/**
 * Summary of a Moodle messaging conversation, including the latest message
 * and unread count. Type 1 = private, type 2 = group.
 */
public record Conversation(
        long id,
        String name,
        int type,
        Integer unreadCount,
        boolean isRead,
        boolean isFavourite,
        boolean isMuted,
        List<ConversationMember> members,
        List<ConversationMessage> messages
) {}

