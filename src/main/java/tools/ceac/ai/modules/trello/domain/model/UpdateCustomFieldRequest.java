package tools.ceac.ai.modules.trello.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Partial update request for a custom field definition.
 *
 * <p>Null fields are not sent to Trello.
 */
public record UpdateCustomFieldRequest(
        @Schema(description = "Field name. Null leaves unchanged. Empty string is forwarded.", example = "Severity")
        String name,
        @Schema(description = "Field position. Null leaves unchanged. Blank values are ignored.", example = "top")
        String position,
        @Schema(description = "Whether to show the field on the card front. Null leaves unchanged.", example = "false")
        Boolean displayCardFront
) {
    public boolean hasChanges() {
        return name != null || position != null || displayCardFront != null;
    }
}
