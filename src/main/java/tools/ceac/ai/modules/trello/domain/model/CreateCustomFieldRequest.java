package tools.ceac.ai.modules.trello.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request to create a new custom field definition on a Trello board.
 *
 * <p>Supported types: {@code text}, {@code number}, {@code date}, {@code checkbox}, {@code list}.
 */
public record CreateCustomFieldRequest(
        @Schema(description = "Board identifier where the field will be created", example = "67f0f9abf6d2b44e9e123456")
        @NotBlank String boardId,
        @Schema(description = "Field name", example = "Priority")
        @NotBlank String name,
        @Schema(description = "Field type: text, number, date, checkbox or list", example = "list")
        @NotBlank String type,
        @Schema(description = "Field position. Typical values: top, bottom or a numeric string.", example = "bottom")
        String position,
        @Schema(description = "Whether to show this field on the card front. Defaults to true when null.", example = "true")
        Boolean displayCardFront
) {
}
