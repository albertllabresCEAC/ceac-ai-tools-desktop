package tools.ceac.ai.modules.campus.interfaces.api.dto;

public record QuizQuestionCategoryResponse(
        /** Numeric category ID (e.g. "117056"). */
        String id,
        /** Human-readable category name. */
        String name,
        /** Section this category belongs to (e.g. "Curso: 02DAM.B_ADATOS"). */
        String group,
        /**
         * Full category value in Moodle format: {@code "categoryId,contextId"}.
         * Pass this as the {@code category} parameter when creating a question.
         */
        String categoryValue
) {}


