package tools.ceac.ai.mcp.campus.infrastructure.campus;

import tools.ceac.ai.mcp.campus.domain.model.SubmissionSummary;

import java.util.List;

/**
 * Transient result of parsing the Moodle assign grading page HTML.
 */
public record AssignGradingPage(
        String contextId,
        String formUserId,
        String sesskey,
        int currentPerpage,
        List<SubmissionSummary> submissions
) {
}
