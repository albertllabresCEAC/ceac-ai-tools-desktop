package tools.ceac.ai.modules.outlook.domain.model;

import org.springframework.ai.tool.annotation.ToolParam;

public class MessageListToolRequest {

    @ToolParam(required = false, description = "Folder to list. Allowed values: INBOX, DRAFTS, SENT, OUTBOX, DELETED. Defaults to INBOX.")
    private String folder;

    @ToolParam(required = false, description = "Maximum number of messages to return. Recommended range is 1 to 25.")
    private Integer limit;

    @ToolParam(required = false, description = "When true, returns only unread messages. Defaults to false.")
    private Boolean unreadOnly;

    @ToolParam(required = false, description = "Filter messages received on or after this ISO-8601 date-time. If omitted, only the last 90 days are listed.")
    private String since;

    @ToolParam(required = false, description = "Sort direction by received date. Allowed values: desc or asc. Defaults to desc.")
    private String sortOrder;

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Boolean getUnreadOnly() {
        return unreadOnly;
    }

    public void setUnreadOnly(Boolean unreadOnly) {
        this.unreadOnly = unreadOnly;
    }

    public String getSince() {
        return since;
    }

    public void setSince(String since) {
        this.since = since;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }
}


