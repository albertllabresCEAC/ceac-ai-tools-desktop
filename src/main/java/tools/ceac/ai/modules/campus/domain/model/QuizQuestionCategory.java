package tools.ceac.ai.modules.campus.domain.model;

/**
 * A question bank category visible in the quiz question bank.
 *
 * @param id            Moodle category ID (e.g. "117056")
 * @param name          Human-readable name, whitespace-trimmed
 * @param group         Section header this category belongs to
 *                      (e.g. "Cuestionario: Test evaluable UD5-UD6" or "Curso: 02DAM.B_ADATOS")
 * @param categoryValue Full category value for the Moodle POST: {@code "categoryId,contextId"}
 *                      (e.g. "117056,379267"). Pass this value when creating a question.
 */
public record QuizQuestionCategory(String id, String name, String group, String categoryValue) {}


