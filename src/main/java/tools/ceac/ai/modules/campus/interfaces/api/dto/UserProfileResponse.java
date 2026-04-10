package tools.ceac.ai.modules.campus.interfaces.api.dto;

import java.util.List;

public record UserProfileResponse(
        String userId,
        String fullName,
        String email,
        String country,
        String timezone,
        List<CourseRefResponse> courses,
        String firstAccess,
        String lastAccess
) {}

