package tools.ceac.ai.mcp.campus.interfaces.api.dto;

public record SaveQuizUserOverrideRequest(
        String userId,
        String password,
        String timeopen,
        String timeclose,
        Long timelimitSeconds,
        Integer attempts
) {}