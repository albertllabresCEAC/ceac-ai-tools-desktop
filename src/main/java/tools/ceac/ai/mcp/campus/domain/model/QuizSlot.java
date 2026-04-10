package tools.ceac.ai.mcp.campus.domain.model;

/**
 * A single slot in a quiz's question order, as seen in {@code mod/quiz/edit.php}.
 *
 * <p>For random slots, {@code questionName} contains the bank-category label and
 * {@code questionText} is null; {@code questionType} is {@code "random"}.
 * For fixed slots both fields are populated from the page.
 */
public record QuizSlot(
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