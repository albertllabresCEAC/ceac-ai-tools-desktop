package tools.ceac.ai.mcp.campus.interfaces.api.dto;

import java.util.List;

public record QuizQuestionDetailResponse(
        String questionId,
        String cmid,
        String category,
        String name,
        String questionText,
        String status,
        String defaultMark,
        String idNumber,
        String single,
        String shuffleAnswers,
        String answerNumbering,
        String showStandardInstruction,
        String penalty,
        String generalFeedback,
        String correctFeedback,
        String partiallyCorrectFeedback,
        String incorrectFeedback,
        List<AnswerItemResponse> answers,
        List<String> hints
) {
    public record AnswerItemResponse(String text, String fraction, String feedback) {}
}
