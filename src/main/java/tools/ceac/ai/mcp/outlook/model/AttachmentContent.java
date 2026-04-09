package tools.ceac.ai.mcp.outlook.model;

public record AttachmentContent(
        String fileName,
        String mediaType,
        String base64Content
) {
}
