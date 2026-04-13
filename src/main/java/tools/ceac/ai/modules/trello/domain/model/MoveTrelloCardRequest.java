package tools.ceac.ai.modules.trello.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Explicit card move request.
 *
 * <p>Use this request when the intent is to move a card between lists. It is clearer than using
 * {@code listId} inside a generic card update.
 */
public record MoveTrelloCardRequest(
        @Schema(description = "Destination Trello list identifier", example = "67f0f8b0f6d2b44e9e999999")
        @NotBlank String listId,
        @Schema(description = "Card position in the destination list. Typical values: top, bottom or a numeric string.",
                example = "top")
        String position
) {
}
