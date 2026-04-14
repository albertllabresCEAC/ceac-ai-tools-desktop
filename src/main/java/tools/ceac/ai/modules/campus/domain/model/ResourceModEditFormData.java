package tools.ceac.ai.modules.campus.domain.model;

import java.util.Map;

public record ResourceModEditFormData(
        Map<String, String> formFields,
        String filesItemId,
        String clientId,
        String contextId,
        String author,
        String uploadRepositoryId
) {
}
