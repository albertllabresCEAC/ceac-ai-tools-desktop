package tools.ceac.ai.mcp.campus.interfaces.api.dto;

import java.util.List;

public record CourseDetailResponse(
        String id,
        int numsections,
        List<SectionResponse> sections
) {
}
