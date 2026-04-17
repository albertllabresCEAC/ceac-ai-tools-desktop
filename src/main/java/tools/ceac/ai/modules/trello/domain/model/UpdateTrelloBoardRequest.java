package tools.ceac.ai.modules.trello.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Partial board update request.
 *
 * <p>Null fields are not sent to Trello. An empty string for {@code name} or {@code desc} is
 * forwarded and may clear that field.
 * Setting {@code closed=true} archives the board; {@code closed=false} unarchives it.
 */
public record UpdateTrelloBoardRequest(
        @Schema(description = "Board name. Null leaves unchanged. Empty string is forwarded.",
                example = "Sprint 42 — closed")
        String name,
        @Schema(description = "Board description. Null leaves unchanged. Empty string is forwarded.",
                example = "Finished sprint")
        String desc,
        @Schema(description = "Set true to archive the board, false to unarchive. Null leaves unchanged.",
                example = "false")
        Boolean closed
) {
    public boolean hasChanges() {
        return name != null || desc != null || closed != null;
    }
}
