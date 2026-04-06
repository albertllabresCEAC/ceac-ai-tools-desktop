package com.alber.outlookdesktop.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class MessageQuery {

    private FolderType folder = FolderType.INBOX;

    @Min(1)
    @Max(200)
    private Integer limit;

    private boolean unreadOnly;

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
}
