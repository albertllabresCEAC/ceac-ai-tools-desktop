package tools.ceac.ai.modules.campus.domain.model;

/** Per-question result summary within a {@link QuizAttempt} (slot, score, state, review link). */
public record QuizQuestionResult(
        String slot,
        String valor,
        String estado,
        String reviewUrl
) {}


