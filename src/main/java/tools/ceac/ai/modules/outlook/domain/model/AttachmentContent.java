package tools.ceac.ai.modules.outlook.domain.model;

public record AttachmentContent(
        String fileName,
        String mediaType,
        String base64Content
) {
}


