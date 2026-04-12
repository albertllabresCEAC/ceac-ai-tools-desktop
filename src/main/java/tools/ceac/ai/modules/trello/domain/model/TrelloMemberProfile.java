package tools.ceac.ai.modules.trello.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TrelloMemberProfile(
        String id,
        String username,
        String fullName,
        String initials,
        String url
) {
}
