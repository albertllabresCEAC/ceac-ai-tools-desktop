package tools.ceac.ai.modules.campus.interfaces.api.dto;

public record FileDownloadResponse(
        String filename,
        String mimeType,
        String content
) {
}


