package tools.ceac.ai.mcp.campus.interfaces.api.dto;

public record ProfileResponse(
        String displayName,
        String userId,
        String email,
        String language,
        int unreadMessages
) {
}
