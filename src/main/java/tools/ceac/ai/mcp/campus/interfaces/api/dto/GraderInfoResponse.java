package tools.ceac.ai.mcp.campus.interfaces.api.dto;

public record GraderInfoResponse(
        String assignmentId,
        String contextId,
        String courseId
) {
}
