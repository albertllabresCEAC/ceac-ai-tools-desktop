package tools.ceac.ai.modules.trello.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TrelloCustomFieldDisplay(
        @JsonProperty("cardFront") Boolean cardFront
) {
}
