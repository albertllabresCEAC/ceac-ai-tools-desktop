package tools.ceac.ai.modules.trello.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

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
