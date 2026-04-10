package tools.ceac.ai.mcp.campus.domain.model;

/**
 * Data extracted from the Moodle question bank delete confirmation page.
 * Used as intermediate state between step 1 and step 2 of the deletion flow.
 */
public record DeleteQuestionConfirmData(
        String confirm,
        String deleteSelected,
        String sesskey,
        String returnUrl,
        String cmid,
        String courseId
) {}