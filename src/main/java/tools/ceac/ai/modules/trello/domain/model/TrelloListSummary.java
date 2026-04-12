package tools.ceac.ai.modules.trello.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TrelloListSummary(
        String id,
        String idBoard,
        String name,
        boolean closed,
        Double pos
) {
}
