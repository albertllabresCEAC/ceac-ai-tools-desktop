package tools.ceac.ai.modules.trello.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

/**
 * An option entry for a list-type Trello custom field.
 * {@code value} contains a single key {@code "text"} with the display label.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TrelloCustomFieldOption(
        String id,
        String idCustomField,
        Map<String, String> value,
        String color,
        Double pos
) {
}
