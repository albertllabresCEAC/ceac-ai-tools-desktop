package tools.ceac.ai.modules.outlook.domain.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.ai.tool.annotation.ToolParam;

public class AttachmentContentToolRequest {

    @NotBlank
    @ToolParam(description = "Outlook entryId of the message that contains the attachment.")
    private String entryId;

    @Min(0)
    @ToolParam(description = "1-based attachment index as returned by listAttachments. Use listAttachments first to discover the valid values.")
    private Integer attachmentIndex;

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public Integer getAttachmentIndex() {
        return attachmentIndex;
    }

    public void setAttachmentIndex(Integer attachmentIndex) {
        this.attachmentIndex = attachmentIndex;
    }
}


