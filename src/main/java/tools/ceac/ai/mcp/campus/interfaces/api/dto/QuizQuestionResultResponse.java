package tools.ceac.ai.mcp.campus.interfaces.api.dto;

public record QuizQuestionResultResponse(
        String slot,
        String valor,
        String estado,
        String reviewUrl
) {}