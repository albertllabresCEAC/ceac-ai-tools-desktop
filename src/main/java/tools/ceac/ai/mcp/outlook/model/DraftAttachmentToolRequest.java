package tools.ceac.ai.mcp.outlook.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.ai.tool.annotation.ToolParam;

public class DraftAttachmentToolRequest {

    @NotBlank
    @ToolParam(description = "Outlook entryId of the draft that will receive the attachment.")
    private String entryId;

    @Valid
    @NotNull
    @ToolParam(description = "Attachment object with fileName, base64Content and optional mediaType.")
    private AttachmentPayload attachment;

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public AttachmentPayload getAttachment() {
        return attachment;
    }

    public void setAttachment(AttachmentPayload attachment) {
        this.attachment = attachment;
    }
}
