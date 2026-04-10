package tools.ceac.ai.modules.campus.interfaces.api.dto;

public record ParticipantResponse(
        String userId,
        String fullName,
        String email,
        String roles,
        String groups,
        String lastAccess
) {}

