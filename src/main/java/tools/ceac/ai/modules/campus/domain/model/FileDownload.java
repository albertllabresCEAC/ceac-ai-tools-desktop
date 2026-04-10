package tools.ceac.ai.modules.campus.domain.model;

/** A downloaded file with its content encoded as a base64 string. */
public record FileDownload(
        String filename,
        String mimeType,
        String content
) {
}


