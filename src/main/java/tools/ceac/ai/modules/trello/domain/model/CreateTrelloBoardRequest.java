package tools.ceac.ai.modules.trello.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request used by the local Trello wrapper to create a new board.
 */
public record CreateTrelloBoardRequest(
        @Schema(description = "Board name", example = "Sprint 42")
        @NotBlank String name,
        @Schema(description = "Board description", example = "Backend tasks for sprint 42")
        String desc,
        @Schema(description = "Whether to pre-populate the board with the default lists (To Do, Doing, Done). Defaults to true when null.",
                example = "true")
        Boolean defaultLists
) {
}
