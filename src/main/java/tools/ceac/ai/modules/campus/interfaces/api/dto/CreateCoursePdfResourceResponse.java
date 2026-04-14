package tools.ceac.ai.modules.campus.interfaces.api.dto;

public record CreateCoursePdfResourceResponse(
        String courseId,
        Integer section,
        String name,
        String fileName,
        String resourceUrl
) {
}
