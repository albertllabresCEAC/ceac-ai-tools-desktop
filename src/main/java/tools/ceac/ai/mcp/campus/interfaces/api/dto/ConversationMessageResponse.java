package tools.ceac.ai.mcp.campus.interfaces.api.dto;

public record ConversationMessageResponse(
        long id,
        long userIdFrom,
        String text,
        long timeCreated
) {}