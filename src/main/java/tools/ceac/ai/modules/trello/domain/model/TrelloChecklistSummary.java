package tools.ceac.ai.modules.trello.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TrelloChecklistSummary(
        String id,
        String idBoard,
        String idCard,
        String name,
        Double pos,
        List<TrelloCheckItemSummary> checkItems
) {
}
