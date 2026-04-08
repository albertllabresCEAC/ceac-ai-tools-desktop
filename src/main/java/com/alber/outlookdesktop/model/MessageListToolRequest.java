package com.alber.outlookdesktop.model;

import org.springframework.ai.tool.annotation.ToolParam;

public class MessageListToolRequest {

    @ToolParam(required = false, description = "Folder to list. Allowed values: INBOX, DRAFTS, SENT, OUTBOX, DELETED. Defaults to INBOX.")
    private String folder;

    @ToolParam(required = false, description = "Maximum number of messages to return. Recommended range is 1 to 25.")
    private Integer limit;

    @ToolParam(required = false, description = "When true, returns only unread messages. Defaults to false.")
    private Boolean unreadOnly;

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
