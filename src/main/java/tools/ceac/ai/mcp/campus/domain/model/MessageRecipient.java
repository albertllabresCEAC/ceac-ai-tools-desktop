package tools.ceac.ai.mcp.campus.domain.model;

/** A potential message recipient (student) across all courses. */
public record MessageRecipient(
        String id,
        String fullName
) {}