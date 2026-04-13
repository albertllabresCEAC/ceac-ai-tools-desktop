package tools.ceac.ai.modules.trello.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Partial checklist item update request.
 *
 * <p>Null fields are not sent to Trello. An empty string for {@code name} is forwarded. Blank
 * values for {@code position} and {@code checklistId} are ignored by this wrapper.
 */
public record UpdateTrelloCheckItemRequest(
        @Schema(description = "Checklist item text. Null leaves unchanged. Empty string is forwarded.",
                example = "Validate final screenshots")
        String name,
        @Schema(description = "Completion state. true maps to complete; false maps to incomplete.",
                example = "true")
        Boolean checked,
        @Schema(description = "Item position. Null leaves unchanged. Blank values are ignored by this wrapper.",
                example = "top")
        String position,
        @Schema(description = "Destination checklist identifier. If set, Trello moves the item to that checklist.",
                example = "67f0f9abf6d2b44e9e654321")
        String checklistId
) {
    public boolean hasChanges() {
        return name != null
                || checked != null
                || position != null
                || checklistId != null;
    }
}
