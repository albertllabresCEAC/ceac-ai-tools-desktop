package tools.ceac.ai.modules.campus.interfaces.api.dto;

public record QuizSlotResponse(
        String slotId,
        int slotNumber,
        int page,
        String questionType,
        String questionName,
        String questionText,
        String maxMark,
        String bankCategoryUrl,
        String questionId
) {}

