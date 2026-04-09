package tools.ceac.ai.mcp.outlook.service;

public class OutlookComException extends RuntimeException {

    public OutlookComException(String message) {
        super(message);
    }

    public OutlookComException(String message, Throwable cause) {
        super(message, cause);
    }
}
