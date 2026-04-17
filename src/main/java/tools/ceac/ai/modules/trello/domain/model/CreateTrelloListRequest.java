package tools.ceac.ai.modules.trello.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request used by the local Trello wrapper to create a new list inside a board.
 */
public record CreateTrelloListRequest(
        @Schema(description = "Board identifier where the list will be created", example = "67f0f9abf6d2b44e9e123456")
        @NotBlank String boardId,
        @Schema(description = "List name", example = "In Review")
        @NotBlank String name,
        @Schema(description = "List position. Typical values: top, bottom or a numeric string.", example = "bottom")
        String position
) {
}
