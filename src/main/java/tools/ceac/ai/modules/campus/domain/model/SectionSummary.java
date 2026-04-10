package tools.ceac.ai.modules.campus.domain.model;

import java.util.List;

/** A course section with its activities and optional nested child sections. */
public record SectionSummary(
        String id,
        String title,
        int number,
        boolean visible,
        String sectionUrl,
        List<ActivitySummary> activities,
        List<SectionSummary> children
) {
}


