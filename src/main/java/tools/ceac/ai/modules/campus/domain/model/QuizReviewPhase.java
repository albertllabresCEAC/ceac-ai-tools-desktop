package tools.ceac.ai.modules.campus.domain.model;

/** Review options enabled for one of the four quiz review phases. */
public record QuizReviewPhase(
        boolean attempt,
        boolean correctness,
        boolean marks,
        boolean specificFeedback,
        boolean generalFeedback,
        boolean rightAnswer,
        boolean overallFeedback
) {}


