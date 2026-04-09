package tools.ceac.ai.mcp.outlook.model;

public record MailDraftResponse(
        String entryId,
        String subject,
        String message
) {
}
