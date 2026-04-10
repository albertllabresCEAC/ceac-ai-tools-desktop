package tools.ceac.ai.mcp.campus.interfaces.api.dto;

public record ReplyToMessageRequest(
        String studentUserId,
        String myUserId,
        String messageTimestamp,
        String messageId,
        String subject,
        String content
) {}