package tools.ceac.ai.modules.trello.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request to add an option to a list-type Trello custom field.
 */
public record CreateCustomFieldOptionRequest(
        @Schema(description = "Display label for the option", example = "High")
        @NotBlank String text,
        @Schema(description = "Option color. Valid values: none, black, blue, green, lime, orange, pink, purple, red, sky, yellow.",
                example = "red")
        String color,
        @Schema(description = "Option position. Typical values: top, bottom or a numeric string.", example = "bottom")
        String position
) {
}
