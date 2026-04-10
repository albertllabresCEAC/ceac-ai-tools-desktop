package tools.ceac.ai.modules.outlook.domain.model;

import jakarta.validation.constraints.NotBlank;
import org.springframework.ai.tool.annotation.ToolParam;

public class AttachmentPayload {

    @NotBlank
    @ToolParam(description = "File name including extension, for example invoice.pdf.")
    private String fileName;

    @ToolParam(required = false, description = "Optional MIME type such as application/pdf or image/png.")
    private String mediaType;

    @NotBlank
    @ToolParam(description = "Attachment content encoded as a Base64 string.")
    private String base64Content;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getBase64Content() {
        return base64Content;
    }

    public void setBase64Content(String base64Content) {
        this.base64Content = base64Content;
    }
}


