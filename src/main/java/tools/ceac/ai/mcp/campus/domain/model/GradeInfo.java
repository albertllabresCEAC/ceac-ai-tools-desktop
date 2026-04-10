package tools.ceac.ai.mcp.campus.domain.model;

/** Current grade value and written feedback for an assignment submission. */
public record GradeInfo(
        String grade,
        String feedback
) {
}
