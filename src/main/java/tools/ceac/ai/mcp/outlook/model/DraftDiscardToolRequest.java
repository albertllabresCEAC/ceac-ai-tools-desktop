package tools.ceac.ai.mcp.outlook.model;

import jakarta.validation.constraints.NotBlank;
import org.springframework.ai.tool.annotation.ToolParam;

public class DraftDiscardToolRequest {

    @NotBlank
    @ToolParam(description = "Outlook entryId of the draft to discard.")
    private String entryId;

    @ToolParam(required = false, description = "When true, permanently deletes the draft. When false or omitted, moves it to Deleted Items.")
    private Boolean permanent;

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public Boolean getPermanent() {
        return permanent;
    }

    public void setPermanent(Boolean permanent) {
        this.permanent = permanent;
    }
}
