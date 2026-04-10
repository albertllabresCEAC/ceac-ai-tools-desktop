package tools.ceac.ai.mcp.campus.interfaces.api.dto;

public record QuizConfigResponse(
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

        // Review options
        QuizReviewPhaseResponse reviewDuringAttempt,
        QuizReviewPhaseResponse reviewImmediatelyAfter,
        QuizReviewPhaseResponse reviewLaterWhileOpen,
        QuizReviewPhaseResponse reviewAfterClose,

        // Display
        int decimalpoints,
        int questiondecimalpoints,

        // Extra restrictions
        String quizpassword,
        String browsersecurity,
        boolean requireSafeExamBrowser
) {}