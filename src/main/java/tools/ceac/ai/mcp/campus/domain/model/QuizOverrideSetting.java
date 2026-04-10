package tools.ceac.ai.mcp.campus.domain.model;

/** A single setting within a {@link QuizUserOverride}, displayed as a human-readable label/value pair. */
public record QuizOverrideSetting(String setting, String value) {}