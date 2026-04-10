package tools.ceac.ai.modules.campus.interfaces.api.dto;

public record ConversationMessageResponse(
        long id,
        long userIdFrom,
        String text,
        long timeCreated
) {}

