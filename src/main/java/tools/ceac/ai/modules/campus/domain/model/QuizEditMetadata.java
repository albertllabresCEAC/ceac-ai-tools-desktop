package tools.ceac.ai.modules.campus.domain.model;

/**
 * Metadata extracted from the quiz edit page ({@code /mod/quiz/edit.php?cmid=XXXX})
 * needed to call Moodle's quiz management REST endpoints.
 */
public record QuizEditMetadata(String sesskey, String courseId, String quizId) {}


