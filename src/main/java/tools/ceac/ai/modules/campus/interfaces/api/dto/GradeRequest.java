package tools.ceac.ai.modules.campus.interfaces.api.dto;

public record GradeRequest(
        int grade,
        String feedback,
        boolean sendNotification
) {
}


