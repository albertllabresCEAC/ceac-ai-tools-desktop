package com.alber.outlookdesktop.model;

import jakarta.validation.constraints.NotBlank;
import org.springframework.ai.tool.annotation.ToolParam;

public class AttachmentListToolRequest {

    @NotBlank
    @ToolParam(description = "Outlook entryId of the message whose attachments should be listed.")
    private String entryId;

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }
}
