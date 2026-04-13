package tools.ceac.ai.modules.trello.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Partial checklist update request.
 *
 * <p>Null fields are not sent to Trello. An empty string for {@code name} is forwarded and may
 * clear the checklist name.
 */
public record UpdateTrelloChecklistRequest(
        @Schema(description = "Checklist name. Null leaves unchanged. Empty string is forwarded.",
                example = "Release validation")
        String name,
        @Schema(description = "Checklist position. Null leaves unchanged. Blank values are ignored by this wrapper.",
                example = "bottom")
        String position
) {
    public boolean hasChanges() {
        return name != null || position != null;
    }
}
