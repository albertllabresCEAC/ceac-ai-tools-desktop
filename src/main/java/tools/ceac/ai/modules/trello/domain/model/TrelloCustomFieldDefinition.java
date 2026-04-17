package tools.ceac.ai.modules.trello.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Definition of a custom field attached to a Trello board.
 *
 * <p>Supported types: {@code text}, {@code number}, {@code date}, {@code checkbox}, {@code list}.
 * The {@code options} list is only present for {@code list}-type fields.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TrelloCustomFieldDefinition(
        String id,
        String idModel,
        String name,
        String type,
        Double pos,
        TrelloCustomFieldDisplay display,
        List<TrelloCustomFieldOption> options
) {
}
