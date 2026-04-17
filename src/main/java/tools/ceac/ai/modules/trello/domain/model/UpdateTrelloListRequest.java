package tools.ceac.ai.modules.trello.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Partial list update request.
 *
 * <p>Null fields are not sent to Trello. An empty string for {@code name} is forwarded and may
 * clear the list name.
 * Setting {@code closed=true} archives the list; {@code closed=false} unarchives it.
 * Providing {@code boardId} moves the list to that board.
 */
public record UpdateTrelloListRequest(
        @Schema(description = "List name. Null leaves unchanged. Empty string is forwarded.",
                example = "Done")
        String name,
        @Schema(description = "Set true to archive the list, false to unarchive. Null leaves unchanged.",
                example = "false")
        Boolean closed,
        @Schema(description = "List position. Null leaves unchanged. Blank values are ignored by this wrapper.",
                example = "top")
        String position,
        @Schema(description = "Target board identifier to move the list to. Null leaves unchanged.",
                example = "67f0f9abf6d2b44e9e654321")
        String boardId
) {
    public boolean hasChanges() {
        return name != null || closed != null || position != null || boardId != null;
    }
}
