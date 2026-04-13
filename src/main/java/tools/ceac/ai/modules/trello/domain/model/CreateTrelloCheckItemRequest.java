package tools.ceac.ai.modules.trello.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request used by the local Trello wrapper to create an item inside a checklist.
 */
public record CreateTrelloCheckItemRequest(
        @Schema(description = "Checklist item text", example = "Validate final screenshots")
        @NotBlank String name,
        @Schema(description = "Initial completion state. true maps to complete; false maps to incomplete.", example = "false")
        Boolean checked,
        @Schema(description = "Item position. Typical values: top, bottom or a numeric string.", example = "bottom")
        String position
) {
}
