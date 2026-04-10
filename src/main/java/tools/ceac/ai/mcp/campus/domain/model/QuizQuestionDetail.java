package tools.ceac.ai.mcp.campus.domain.model;

import java.util.List;

public record QuizQuestionDetail(
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
        List<QuizQuestionAnswer> answers,
        List<String> hints
) {}
