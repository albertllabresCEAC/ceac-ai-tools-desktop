package tools.ceac.ai.modules.campus.interfaces.api.dto;

public record CreateCourseAssignmentResponse(
        String courseId,
        Integer section,
        String name,
        String assignmentId,
        String assignmentUrl,
        String courseUrl
) {
}
