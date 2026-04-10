package tools.ceac.ai.modules.campus.domain.model;

/** A potential message recipient (student) across all courses. */
public record MessageRecipient(
        String id,
        String fullName
) {}

