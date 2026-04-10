package tools.ceac.ai.modules.campus.domain.model;

import java.util.List;

/** Full course structure as returned by the {@code core_courseformat_get_state} API. */
public record CourseDetail(
        String id,
        int numsections,
        List<SectionSummary> sections
) {
}


