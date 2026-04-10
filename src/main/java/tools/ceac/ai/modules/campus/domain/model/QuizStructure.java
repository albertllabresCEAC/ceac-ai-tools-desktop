package tools.ceac.ai.modules.campus.domain.model;

import java.util.List;

/** The slot structure of a quiz as seen in {@code mod/quiz/edit.php?cmid=XXXX}. */
public record QuizStructure(
        String cmid,
        int totalQuestions,
        String totalMarks,
        String maxGrade,
        List<QuizSlot> slots
) {}

