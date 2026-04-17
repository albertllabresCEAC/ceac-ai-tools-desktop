package tools.ceac.ai.modules.trello.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Simplified Trello card projection used by local API and MCP responses.
 *
 * @param id card identifier
 * @param idBoard board identifier
 * @param idList list identifier
 * @param name card name
 * @param desc card description
 * @param url Trello web URL
 * @param closed whether the card is archived
 * @param due due datetime when present
 * @param dueComplete whether the due date is marked as complete
 * @param pos Trello position value
 * @param idMembers member ids assigned to the card
 * @param customFieldItems current custom field values returned by Trello for the card
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TrelloCardSummary(
        String id,
        String idBoard,
        String idList,
        String name,
        String desc,
        String url,
        boolean closed,
        String due,
        Boolean dueComplete,
        Double pos,
        List<String> idMembers,
        List<TrelloCustomFieldItem> customFieldItems
) {
}
