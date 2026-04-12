package tools.ceac.ai.modules.trello.application.service;

import java.util.List;
import org.springframework.stereotype.Service;
import tools.ceac.ai.modules.trello.application.auth.TrelloRuntimeCredentials;
import tools.ceac.ai.modules.trello.domain.model.CreateTrelloCardRequest;
import tools.ceac.ai.modules.trello.domain.model.TrelloBoardSummary;
import tools.ceac.ai.modules.trello.domain.model.TrelloCardSummary;
import tools.ceac.ai.modules.trello.domain.model.TrelloListSummary;
import tools.ceac.ai.modules.trello.domain.model.TrelloMemberProfile;
import tools.ceac.ai.modules.trello.domain.model.TrelloStatusResponse;
import tools.ceac.ai.modules.trello.infrastructure.trello.TrelloHttpClient;

@Service
public class TrelloService {

    private final TrelloRuntimeCredentials credentials;
    private final TrelloHttpClient httpClient;

    public TrelloService(TrelloRuntimeCredentials credentials, TrelloHttpClient httpClient) {
        this.credentials = credentials;
        this.httpClient = httpClient;
    }

    public TrelloStatusResponse status() {
        credentials.assertConfigured();
        return new TrelloStatusResponse(true, credentials.apiBaseUrl(), getCurrentMember());
    }

    public TrelloMemberProfile getCurrentMember() {
        return httpClient.getCurrentMember();
    }

    public List<TrelloBoardSummary> listBoards() {
        return httpClient.listBoards();
    }

    public List<TrelloListSummary> listLists(String boardId) {
        return httpClient.listLists(boardId);
    }

    public List<TrelloCardSummary> listCards(String listId) {
        return httpClient.listCards(listId);
    }

    public TrelloCardSummary createCard(CreateTrelloCardRequest request) {
        return httpClient.createCard(request);
    }
}
