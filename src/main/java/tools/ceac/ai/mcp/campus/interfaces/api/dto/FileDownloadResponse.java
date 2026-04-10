package tools.ceac.ai.mcp.campus.interfaces.api.dto;

public record FileDownloadResponse(
        String filename,
        String mimeType,
        String content
) {
}
