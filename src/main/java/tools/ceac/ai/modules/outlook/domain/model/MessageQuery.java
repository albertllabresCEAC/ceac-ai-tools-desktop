package tools.ceac.ai.modules.outlook.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.OffsetDateTime;

public class MessageQuery {

    @Schema(defaultValue = "INBOX")
    private FolderType folder = FolderType.INBOX;

    @Min(1)
    @Max(200)
    @Schema(defaultValue = "20")
    private Integer limit = 20;

    @Schema(defaultValue = "false")
    private boolean unreadOnly;

    @Schema(description = "Filter messages received on or after this date-time. Defaults to now minus 7 days when omitted.")
    private OffsetDateTime since = OffsetDateTime.now().minusDays(7);

    @Schema(defaultValue = "desc", allowableValues = {"desc", "asc"})
    private String sortOrder = "desc";

    public FolderType getFolder() {
        return folder;
    }

    public void setFolder(FolderType folder) {
        this.folder = folder;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public boolean isUnreadOnly() {
        return unreadOnly;
    }

    public void setUnreadOnly(boolean unreadOnly) {
        this.unreadOnly = unreadOnly;
    }

    public OffsetDateTime getSince() {
        return since;
    }

    public void setSince(OffsetDateTime since) {
        this.since = since;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }
}


