package tools.ceac.ai.modules.trello.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TrelloCheckItemSummary(
        String id,
        String idChecklist,
        String name,
        String state,
        Double pos,
        String due,
        Integer dueReminder,
        String idMember
) {
}
