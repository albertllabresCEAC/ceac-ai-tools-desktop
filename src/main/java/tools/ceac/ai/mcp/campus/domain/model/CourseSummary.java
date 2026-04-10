package tools.ceac.ai.mcp.campus.domain.model;

/**
 * Course projection in dashboard context.
 */
public record CourseSummary(
        String id,
        String name,
        String url
) {
}
