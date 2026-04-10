package tools.ceac.ai.modules.campus.domain.model;

/** Current grade value and written feedback for an assignment submission. */
public record GradeInfo(
        String grade,
        String feedback
) {
}


