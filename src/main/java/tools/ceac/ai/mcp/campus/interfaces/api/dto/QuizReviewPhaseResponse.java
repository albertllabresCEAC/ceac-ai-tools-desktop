package tools.ceac.ai.mcp.campus.interfaces.api.dto;

public record QuizReviewPhaseResponse(
        boolean attempt,
        boolean correctness,
        boolean marks,
        boolean specificFeedback,
        boolean generalFeedback,
        boolean rightAnswer,
        boolean overallFeedback
) {}