package tools.ceac.ai.mcp.campus.domain.model;

import java.util.List;

/**
 * Data parsed from {@code question/edit.php?cmid=XXXX}.
 *
 * @param courseId           Moodle course ID (e.g. "8824")
 * @param sesskey            Current session key extracted from M.cfg JS
 * @param categories         Available question bank categories for this quiz
 */
public record QuestionBankData(
        String courseId,
        String sesskey,
        List<QuizQuestionCategory> categories
) {}
