package tools.ceac.ai.mcp.campus.interfaces.api.dto;

public record ConversationMemberResponse(
        long id,
        String fullName,
        String profileImageUrl,
        Boolean isOnline,
        boolean isBlocked,
        boolean isContact
) {}