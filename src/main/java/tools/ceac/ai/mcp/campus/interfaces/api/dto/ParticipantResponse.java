package tools.ceac.ai.mcp.campus.interfaces.api.dto;

public record ParticipantResponse(
        String userId,
        String fullName,
        String email,
        String roles,
        String groups,
        String lastAccess
) {}