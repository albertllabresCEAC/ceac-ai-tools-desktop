package tools.ceac.ai.modules.campus.interfaces.api.dto;

public record QuizQuestionResultResponse(
        String slot,
        String valor,
        String estado,
        String reviewUrl
) {}

