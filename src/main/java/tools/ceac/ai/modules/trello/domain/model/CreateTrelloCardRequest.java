package tools.ceac.ai.modules.trello.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request used by the local Trello wrapper to create a new card.
 *
 * <p>Only {@code listId} and {@code name} are required by this wrapper. Optional string fields are
 * omitted from the downstream Trello request when they are {@code null}, empty or blank.
 */
public record CreateTrelloCardRequest(
        @Schema(description = "Destination Trello list identifier", example = "67f0f8b0f6d2b44e9e123456")
        @NotBlank String listId,
        @Schema(description = "Card title", example = "Prepare launch checklist")
        @NotBlank String name,
        @Schema(description = "Card description. Null or blank means 'do not send this field' on create.",
                example = "Collect remaining deployment tasks")
        String description,
        @Schema(description = "Due date in a Trello-compatible datetime format, usually ISO-8601.",
                example = "2026-04-30T18:00:00.000Z")
        String due,
        @Schema(description = "Card position within the list. Typical values: top, bottom or a numeric string.",
                example = "bottom")
        String position
) {
}
