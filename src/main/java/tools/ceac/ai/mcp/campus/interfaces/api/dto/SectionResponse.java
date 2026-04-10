package tools.ceac.ai.mcp.campus.interfaces.api.dto;

import java.util.List;

public record SectionResponse(
        String id,
        String title,
        int number,
        boolean visible,
        String sectionUrl,
        List<ActivityResponse> activities,
        List<SectionResponse> children
) {
}
