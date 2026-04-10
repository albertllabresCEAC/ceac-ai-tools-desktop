package tools.ceac.ai.modules.campus.interfaces.api.dto;

public record QuizReviewPhaseResponse(
        boolean attempt,
        boolean correctness,
        boolean marks,
        boolean specificFeedback,
        boolean generalFeedback,
        boolean rightAnswer,
        boolean overallFeedback
) {}

