package tools.ceac.ai.modules.trello.application.service;

import java.util.List;
import org.springframework.stereotype.Service;
import tools.ceac.ai.modules.trello.application.auth.TrelloRuntimeCredentials;
import tools.ceac.ai.modules.trello.domain.model.CreateCustomFieldOptionRequest;
import tools.ceac.ai.modules.trello.domain.model.CreateCustomFieldRequest;
import tools.ceac.ai.modules.trello.domain.model.CreateTrelloBoardRequest;
import tools.ceac.ai.modules.trello.domain.model.CreateTrelloCheckItemRequest;
import tools.ceac.ai.modules.trello.domain.model.CreateTrelloChecklistRequest;
import tools.ceac.ai.modules.trello.domain.model.CreateTrelloCardRequest;
import tools.ceac.ai.modules.trello.domain.model.CreateTrelloListRequest;
import tools.ceac.ai.modules.trello.domain.model.MoveTrelloCardRequest;
import tools.ceac.ai.modules.trello.domain.model.TrelloBoardSummary;
import tools.ceac.ai.modules.trello.domain.model.TrelloCardSummary;
import tools.ceac.ai.modules.trello.domain.model.TrelloCheckItemSummary;
import tools.ceac.ai.modules.trello.domain.model.TrelloChecklistSummary;
import tools.ceac.ai.modules.trello.domain.model.TrelloCustomFieldDefinition;
import tools.ceac.ai.modules.trello.domain.model.TrelloCustomFieldItem;
import tools.ceac.ai.modules.trello.domain.model.TrelloCustomFieldOption;
import tools.ceac.ai.modules.trello.domain.model.TrelloListSummary;
import tools.ceac.ai.modules.trello.domain.model.TrelloMemberProfile;
import tools.ceac.ai.modules.trello.domain.model.TrelloOperationResult;
import tools.ceac.ai.modules.trello.domain.model.TrelloStatusResponse;
import tools.ceac.ai.modules.trello.domain.model.UpdateCustomFieldItemRequest;
import tools.ceac.ai.modules.trello.domain.model.UpdateCustomFieldRequest;
import tools.ceac.ai.modules.trello.domain.model.UpdateTrelloBoardRequest;
import tools.ceac.ai.modules.trello.domain.model.UpdateTrelloCardRequest;
import tools.ceac.ai.modules.trello.domain.model.UpdateTrelloCheckItemRequest;
import tools.ceac.ai.modules.trello.domain.model.UpdateTrelloChecklistRequest;
import tools.ceac.ai.modules.trello.domain.model.UpdateTrelloListRequest;
import tools.ceac.ai.modules.trello.infrastructure.trello.TrelloHttpClient;

@Service
public class TrelloService {

    private final TrelloRuntimeCredentials credentials;
    private final TrelloHttpClient httpClient;

    public TrelloService(TrelloRuntimeCredentials credentials, TrelloHttpClient httpClient) {
        this.credentials = credentials;
        this.httpClient = httpClient;
    }

    /**
     * Returns the current wrapper status for the connected Trello account.
     */
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

    public TrelloBoardSummary getBoard(String boardId) {
        return httpClient.getBoard(requireId(boardId, "boardId"));
    }

    public TrelloBoardSummary createBoard(CreateTrelloBoardRequest request) {
        return httpClient.createBoard(request);
    }

    public TrelloBoardSummary updateBoard(String boardId, UpdateTrelloBoardRequest request) {
        if (request == null || !request.hasChanges()) {
            throw new IllegalStateException("Debes indicar al menos un cambio para actualizar el tablero.");
        }
        return httpClient.updateBoard(requireId(boardId, "boardId"), request);
    }

    public TrelloOperationResult closeBoard(String boardId) {
        return httpClient.closeBoard(requireId(boardId, "boardId"));
    }

    public List<TrelloListSummary> listLists(String boardId) {
        return httpClient.listLists(boardId);
    }

    public TrelloListSummary getList(String listId) {
        return httpClient.getList(requireId(listId, "listId"));
    }

    public TrelloListSummary createList(CreateTrelloListRequest request) {
        return httpClient.createList(request);
    }

    public TrelloListSummary updateList(String listId, UpdateTrelloListRequest request) {
        if (request == null || !request.hasChanges()) {
            throw new IllegalStateException("Debes indicar al menos un cambio para actualizar la lista.");
        }
        return httpClient.updateList(requireId(listId, "listId"), request);
    }

    public TrelloOperationResult archiveList(String listId) {
        return httpClient.archiveList(requireId(listId, "listId"));
    }

    public List<TrelloCardSummary> listCards(String listId) {
        return httpClient.listCards(listId);
    }

    public TrelloCardSummary createCard(CreateTrelloCardRequest request) {
        return httpClient.createCard(request);
    }

    public TrelloCardSummary getCard(String cardId) {
        return httpClient.getCard(requireId(cardId, "cardId"));
    }

    /**
     * Partially updates a card.
     *
     * <p>Only non-null fields are propagated by the wrapper. Empty strings are still considered
     * explicit values for some fields and may therefore clear data on Trello.
     */
    public TrelloCardSummary updateCard(String cardId, UpdateTrelloCardRequest request) {
        if (request == null || !request.hasChanges()) {
            throw new IllegalStateException("Debes indicar al menos un cambio para actualizar la tarjeta.");
        }
        return httpClient.updateCard(requireId(cardId, "cardId"), request);
    }

    public TrelloOperationResult deleteCard(String cardId) {
        return httpClient.deleteCard(requireId(cardId, "cardId"));
    }

    /**
     * Moves a card to another list.
     *
     * <p>This operation only touches list and optional position. It does not archive the card and
     * does not mark the due date as complete.
     */
    public TrelloCardSummary moveCard(String cardId, MoveTrelloCardRequest request) {
        return httpClient.updateCard(
                requireId(cardId, "cardId"),
                new UpdateTrelloCardRequest(null, null, null, null, request.position(), requireId(request.listId(), "listId"), null)
        );
    }

    public List<TrelloChecklistSummary> listChecklists(String cardId) {
        return httpClient.listChecklists(requireId(cardId, "cardId"));
    }

    public TrelloChecklistSummary createChecklist(String cardId, CreateTrelloChecklistRequest request) {
        return httpClient.createChecklist(requireId(cardId, "cardId"), request);
    }

    public TrelloChecklistSummary updateChecklist(String checklistId, UpdateTrelloChecklistRequest request) {
        if (request == null || !request.hasChanges()) {
            throw new IllegalStateException("Debes indicar al menos un cambio para actualizar la checklist.");
        }
        return httpClient.updateChecklist(requireId(checklistId, "checklistId"), request);
    }

    public TrelloOperationResult deleteChecklist(String checklistId) {
        return httpClient.deleteChecklist(requireId(checklistId, "checklistId"));
    }

    public TrelloCheckItemSummary createCheckItem(String checklistId, CreateTrelloCheckItemRequest request) {
        return httpClient.createCheckItem(requireId(checklistId, "checklistId"), request);
    }

    /**
     * Partially updates a checklist item.
     *
     * <p>When {@code checklistId} is provided, Trello moves the item to that checklist.
     */
    public TrelloCheckItemSummary updateCheckItem(String cardId, String checkItemId, UpdateTrelloCheckItemRequest request) {
        if (request == null || !request.hasChanges()) {
            throw new IllegalStateException("Debes indicar al menos un cambio para actualizar el item de checklist.");
        }
        return httpClient.updateCheckItem(requireId(cardId, "cardId"), requireId(checkItemId, "checkItemId"), request);
    }

    public TrelloOperationResult deleteCheckItem(String cardId, String checkItemId) {
        return httpClient.deleteCheckItem(requireId(cardId, "cardId"), requireId(checkItemId, "checkItemId"));
    }

    // ── Custom Fields ────────────────────────────────────────────────────────

    public List<TrelloCustomFieldDefinition> listCustomFields(String boardId) {
        return httpClient.listCustomFields(requireId(boardId, "boardId"));
    }

    public TrelloCustomFieldDefinition createCustomField(CreateCustomFieldRequest request) {
        return httpClient.createCustomField(request);
    }

    public TrelloCustomFieldDefinition updateCustomField(String customFieldId, UpdateCustomFieldRequest request) {
        if (request == null || !request.hasChanges()) {
            throw new IllegalStateException("Debes indicar al menos un cambio para actualizar el campo personalizado.");
        }
        return httpClient.updateCustomField(requireId(customFieldId, "customFieldId"), request);
    }

    public TrelloOperationResult deleteCustomField(String customFieldId) {
        return httpClient.deleteCustomField(requireId(customFieldId, "customFieldId"));
    }

    public TrelloCustomFieldOption createCustomFieldOption(String customFieldId, CreateCustomFieldOptionRequest request) {
        return httpClient.createCustomFieldOption(requireId(customFieldId, "customFieldId"), request);
    }

    public TrelloOperationResult deleteCustomFieldOption(String customFieldId, String optionId) {
        return httpClient.deleteCustomFieldOption(requireId(customFieldId, "customFieldId"), requireId(optionId, "optionId"));
    }

    public List<TrelloCustomFieldItem> listCardCustomFieldItems(String cardId) {
        return httpClient.listCardCustomFieldItems(requireId(cardId, "cardId"));
    }

    public TrelloCustomFieldItem updateCardCustomFieldItem(String cardId, String customFieldId,
            UpdateCustomFieldItemRequest request) {
        return httpClient.updateCardCustomFieldItem(
                requireId(cardId, "cardId"),
                requireId(customFieldId, "customFieldId"),
                request
        );
    }

    private String requireId(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Falta " + label + ".");
        }
        return value.trim();
    }
}
