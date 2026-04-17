package tools.ceac.ai.modules.trello.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request to set or clear a custom field value on a Trello card.
 *
 * <p>Provide exactly one of the typed value fields matching the field's type,
 * or set {@code clear=true} to remove the value from the card.
 *
 * <ul>
 *   <li>{@code text} — for {@code text} fields</li>
 *   <li>{@code number} — for {@code number} fields (as a string representation)</li>
 *   <li>{@code date} — for {@code date} fields (ISO-8601 datetime)</li>
 *   <li>{@code checked} — for {@code checkbox} fields</li>
 *   <li>{@code idValue} — for {@code list} fields (option id); use empty string to clear</li>
 *   <li>{@code clear=true} — removes the value for any field type</li>
 * </ul>
 */
public record UpdateCustomFieldItemRequest(
        @Schema(description = "Value for a text-type field.", example = "Needs UX review")
        String text,
        @Schema(description = "Value for a number-type field, as a string.", example = "42")
        String number,
        @Schema(description = "Value for a date-type field in ISO-8601 format.", example = "2026-05-01T09:00:00.000Z")
        String date,
        @Schema(description = "Value for a checkbox-type field.", example = "true")
        Boolean checked,
        @Schema(description = "Option id for a list-type field. Pass an empty string to clear the selection.",
                example = "67f0f9abf6d2b44e9e654321")
        String idValue,
        @Schema(description = "Set to true to clear the field value regardless of type.", example = "false")
        boolean clear
) {
}
