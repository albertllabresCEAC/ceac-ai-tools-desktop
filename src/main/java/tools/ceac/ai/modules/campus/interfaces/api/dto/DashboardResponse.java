package tools.ceac.ai.modules.campus.interfaces.api.dto;

import java.util.List;

public record DashboardResponse(
        String pageTitle,
        String userDisplayName,
        List<CourseResponse> courses
) {
}


