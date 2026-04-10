package tools.ceac.ai.mcp.campus.domain.model;

import java.util.List;

/** A quiz user override: individual exception settings for a specific student. */
public record QuizUserOverride(
        String overrideId,
        String userId,
        String fullName,
        String email,
        List<QuizOverrideSetting> settings
) {}