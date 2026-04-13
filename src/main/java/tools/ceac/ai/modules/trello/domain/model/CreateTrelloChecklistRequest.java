package tools.ceac.ai.modules.trello.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request used by the local Trello wrapper to create a checklist inside a card.
 */
public record CreateTrelloChecklistRequest(
        @Schema(description = "Checklist name", example = "Definition of done")
        @NotBlank String name,
        @Schema(description = "Checklist position. Typical values: top, bottom or a numeric string.", example = "bottom")
        String position,
        @Schema(description = "Optional source checklist identifier to clone from", example = "67f0f9abf6d2b44e9e654321")
        String sourceChecklistId
) {
}
