package tools.ceac.ai.modules.trello.domain.model;

public record TrelloStatusResponse(
        boolean connected,
        String apiBaseUrl,
        TrelloMemberProfile member
) {
}
