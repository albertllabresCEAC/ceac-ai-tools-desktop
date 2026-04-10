package tools.ceac.ai.modules.campus.interfaces.api.dto;

import java.util.List;

public record QuizUserOverrideResponse(
        String overrideId,
        String userId,
        String fullName,
        String email,
        List<QuizOverrideSettingResponse> settings
) {}

