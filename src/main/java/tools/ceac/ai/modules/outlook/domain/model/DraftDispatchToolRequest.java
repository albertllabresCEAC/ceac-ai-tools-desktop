package tools.ceac.ai.modules.outlook.domain.model;

import jakarta.validation.constraints.NotBlank;
import org.springframework.ai.tool.annotation.ToolParam;

public class DraftDispatchToolRequest {

    @NotBlank
    @ToolParam(description = "Outlook entryId of the existing draft to send.")
    private String entryId;

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }
}


