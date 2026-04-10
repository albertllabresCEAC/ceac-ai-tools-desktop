package tools.ceac.ai.mcp.campus.domain.model;

/** A single message within a Moodle conversation. {@code timeCreated} is a Unix timestamp. */
public record ConversationMessage(
        long id,
        long userIdFrom,
        String text,
        long timeCreated
) {}