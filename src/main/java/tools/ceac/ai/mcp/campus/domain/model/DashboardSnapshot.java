package tools.ceac.ai.mcp.campus.domain.model;

import java.util.List;

/**
 * Parsed dashboard information used by API layer.
 */
public record DashboardSnapshot(
        String pageTitle,
        String userDisplayName,
        String userId,
        String email,
        String language,
        int unreadMessages,
        List<CourseSummary> courses,
        String sesskey
) {
}
