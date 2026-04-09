package tools.ceac.ai.mcp.outlook.model;

import jakarta.validation.constraints.NotBlank;
import org.springframework.ai.tool.annotation.ToolParam;

public class MessageGetToolRequest {

    @NotBlank
    @ToolParam(description = "Outlook entryId of the message to retrieve.")
    private String entryId;

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }
}
