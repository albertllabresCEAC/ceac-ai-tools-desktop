package tools.ceac.ai.modules.campus.interfaces.api.dto;

import java.util.List;

public record SubmissionResponse(
        String userId,
        String fullName,
        String email,
        String status,
        String submittedAt,
        List<String> files
) {
}


