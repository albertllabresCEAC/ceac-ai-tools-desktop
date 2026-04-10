package tools.ceac.ai.mcp.campus.interfaces.api.dto;

import java.util.List;

public record DashboardResponse(
        String pageTitle,
        String userDisplayName,
        List<CourseResponse> courses
) {
}
