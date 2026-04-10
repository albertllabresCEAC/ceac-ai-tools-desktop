package tools.ceac.ai.modules.campus.interfaces.api.dto;

public record ConversationMemberResponse(
        long id,
        String fullName,
        String profileImageUrl,
        Boolean isOnline,
        boolean isBlocked,
        boolean isContact
) {}

