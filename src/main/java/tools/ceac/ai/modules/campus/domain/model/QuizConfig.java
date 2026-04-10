package tools.ceac.ai.modules.campus.domain.model;

/**
 * Configuration of a Moodle quiz module as read from modedit.php.
 * <p>
 * {@code timeopen} / {@code timeclose} are ISO-8601 local datetime strings
 * (e.g. "2026-02-19T00:00"), or {@code null} when disabled.
 * {@code timelimitSeconds} is {@code null} when there is no time limit.
 * {@code attempts} is 0 for unlimited.
 * </p>
 */
public record QuizConfig(
        String cmid,
        String courseId,
        String name,
        boolean visible,

        // Timing
        String timeopen,
        String timeclose,
        Long timelimitSeconds,
        String overduehandling,

        // Grade
        String gradepass,
        int attempts,
        int grademethod,

        // Layout
        int questionsperpage,
        String navmethod,

        // Question behaviour
        boolean shuffleanswers,
        String preferredbehaviour,

        // Review options â€” four phases
        QuizReviewPhase reviewDuringAttempt,
        QuizReviewPhase reviewImmediatelyAfter,
        QuizReviewPhase reviewLaterWhileOpen,
        QuizReviewPhase reviewAfterClose,

        // Display
        int decimalpoints,
        int questiondecimalpoints,

        // Extra restrictions
        String quizpassword,
        String browsersecurity,
        boolean requireSafeExamBrowser
) {}


