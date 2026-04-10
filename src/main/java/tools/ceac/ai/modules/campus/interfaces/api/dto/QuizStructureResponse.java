package tools.ceac.ai.modules.campus.interfaces.api.dto;

import java.util.List;

public record QuizStructureResponse(
        String cmid,
        int totalQuestions,
        String totalMarks,
        String maxGrade,
        List<QuizSlotResponse> slots
) {}

