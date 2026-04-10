package tools.ceac.ai.mcp.campus.interfaces.api.dto;

public record GradeRequest(
        int grade,
        String feedback,
        boolean sendNotification
) {
}
