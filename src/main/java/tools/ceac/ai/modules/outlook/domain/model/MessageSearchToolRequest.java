package tools.ceac.ai.modules.outlook.domain.model;

import org.springframework.ai.tool.annotation.ToolParam;

public class MessageSearchToolRequest {

    @ToolParam(required = false, description = "Free-text search query matched against subject, sender, recipients and message body. If omitted, returns recent messages from the selected folders.")
    private String query;

    @ToolParam(required = false, description = "Folder to search. Allowed values: INBOX, DRAFTS, SENT, OUTBOX, DELETED, ALL. Defaults to ALL, which searches INBOX, SENT and DRAFTS.")
    private String folder;

    @ToolParam(required = false, description = "Maximum number of matches to return. Recommended range is 1 to 25.")
    private Integer limit;

    @ToolParam(required = false, description = "When true, restricts the search to unread messages only. Defaults to false.")
    private Boolean unreadOnly;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

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
}


