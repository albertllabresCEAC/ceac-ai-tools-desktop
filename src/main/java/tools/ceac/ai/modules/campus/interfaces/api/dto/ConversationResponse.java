package tools.ceac.ai.modules.campus.interfaces.api.dto;

import java.util.List;

public record ConversationResponse(
        long id,
        String name,
        int type,
        Integer unreadCount,
        boolean isRead,
        boolean isFavourite,
        boolean isMuted,
        List<ConversationMemberResponse> members,
        List<ConversationMessageResponse> messages
) {}

