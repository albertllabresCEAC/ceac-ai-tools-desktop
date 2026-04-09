package tools.ceac.ai.mcp.outlook.model;

public enum FolderType {
    INBOX(6),
    DRAFTS(16),
    SENT(5),
    OUTBOX(4),
    DELETED(3);

    private final int comValue;

    FolderType(int comValue) {
        this.comValue = comValue;
    }

    public int getComValue() {
        return comValue;
    }
}
