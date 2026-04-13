package tools.ceac.ai.modules.trello.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Partial card update request for the local Trello wrapper.
 *
 * <p>Wrapper semantics:
 *
 * <ul>
 *   <li>Omitted fields are deserialized as {@code null} and therefore are not sent to Trello.</li>
 *   <li>{@code null} means "leave unchanged".</li>
 *   <li>An empty string is <strong>not</strong> treated as a no-op for {@code name},
 *       {@code description} or {@code due}; if provided, it is forwarded and may clear data or
 *       produce Trello-side validation errors.</li>
 *   <li>{@code closed=true} archives the card.</li>
 *   <li>{@code dueComplete=true} marks the due date as completed without archiving the card.</li>
 * </ul>
 */
public record UpdateTrelloCardRequest(
        @Schema(description = "Card title. Null leaves unchanged. Empty string is forwarded and may clear the title.",
                example = "Prepare launch checklist v2")
        String name,
        @Schema(description = "Card description. Null leaves unchanged. Empty string is forwarded and may clear the description.",
                example = "Updated deployment and QA steps")
        String description,
        @Schema(description = "Due date in a Trello-compatible datetime format. Null leaves unchanged. Empty string is forwarded.",
                example = "2026-05-02T10:30:00.000Z")
        String due,
        @Schema(description = "Marks the due date as complete/incomplete. This does not archive the card.",
                example = "true")
        Boolean dueComplete,
        @Schema(description = "Card position. Null leaves unchanged. Blank values are ignored by this wrapper.",
                example = "top")
        String position,
        @Schema(description = "Destination list identifier. Prefer the explicit move endpoint/tool when the intent is to move a card.",
                example = "67f0f8b0f6d2b44e9e999999")
        String listId,
        @Schema(description = "Archives the card when true. This is different from dueComplete.",
                example = "false")
        Boolean closed
) {
    public boolean hasChanges() {
        return name != null
                || description != null
                || due != null
                || dueComplete != null
                || position != null
                || listId != null
                || closed != null;
    }
}
