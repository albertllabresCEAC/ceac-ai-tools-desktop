package tools.ceac.ai.mcp.campus.domain.model;

import java.util.List;

/**
 * Summary of a student's assignment submission as shown in the grading table.
 * {@code files} contains the download URLs of the attached files.
 */
public record SubmissionSummary(
        String userId,
        String fullName,
        String email,
        String status,
        String submittedAt,
        List<String> files
) {
}
