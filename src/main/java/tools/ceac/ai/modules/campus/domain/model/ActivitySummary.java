package tools.ceac.ai.modules.campus.domain.model;

/**
 * A Moodle activity or resource within a course section.
 * Common types: {@code assign}, {@code quiz}, {@code forum}, {@code resource}, {@code url}, {@code page}.
 */
public record ActivitySummary(
        String id,
        String name,
        String type,
        String url,
        boolean visible
) {
}


