package tools.ceac.ai.modules.trello.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

/**
 * The value of a custom field on a specific Trello card.
 *
 * <p>For {@code text}, {@code number}, {@code date} and {@code checkbox} fields,
 * {@code value} contains a single-key map (e.g. {@code {"text":"foo"}}, {@code {"checked":"true"}}).
 * For {@code list} fields, {@code value} is null and {@code idValue} holds the selected option id.
 * Both {@code value} and {@code idValue} are null when the field has no value set on the card.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TrelloCustomFieldItem(
        String id,
        String idCustomField,
        String idModel,
        Map<String, String> value,
        String idValue
) {
}
