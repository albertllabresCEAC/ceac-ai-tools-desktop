package tools.ceac.ai.modules.trello.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TrelloBoardSummary(
        String id,
        String name,
        String desc,
        String url,
        boolean closed
) {
}
