package tools.ceac.ai.modules.trello.interfaces.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import tools.ceac.ai.modules.trello.application.service.TrelloService;
import tools.ceac.ai.modules.trello.domain.model.CreateTrelloCheckItemRequest;
import tools.ceac.ai.modules.trello.domain.model.CreateTrelloChecklistRequest;
import tools.ceac.ai.modules.trello.domain.model.CreateTrelloCardRequest;
import tools.ceac.ai.modules.trello.domain.model.MoveTrelloCardRequest;
import tools.ceac.ai.modules.trello.domain.model.UpdateTrelloCardRequest;
import tools.ceac.ai.modules.trello.domain.model.UpdateTrelloCheckItemRequest;
import tools.ceac.ai.modules.trello.domain.model.UpdateTrelloChecklistRequest;

@Component
public class TrelloMcpTools {

    private final TrelloService trelloService;
    private final ObjectMapper objectMapper;

    public TrelloMcpTools(TrelloService trelloService, ObjectMapper objectMapper) {
        this.trelloService = trelloService;
        this.objectMapper = objectMapper;
    }

    @Tool(description = "Returns the current Trello connection state used by the local runtime.")
    public String verEstadoTrello() throws Exception {
        return objectMapper.writeValueAsString(trelloService.status());
    }

    @Tool(description = "Lists the boards visible to the connected Trello account.")
    public String listarTablerosTrello() throws Exception {
        return objectMapper.writeValueAsString(trelloService.listBoards());
    }

    @Tool(description = "Lists the lists of a Trello board from its boardId.")
    public String listarListasTrello(String boardId) throws Exception {
        return objectMapper.writeValueAsString(trelloService.listLists(boardId));
    }

    @Tool(description = "Lists the cards of a Trello list from its listId.")
    public String listarTarjetasTrello(String listId) throws Exception {
        return objectMapper.writeValueAsString(trelloService.listCards(listId));
    }

    @Tool(description = "Returns the current detail of a Trello card from its cardId.")
    public String verTarjetaTrello(String cardId) throws Exception {
        return objectMapper.writeValueAsString(trelloService.getCard(cardId));
    }

    @Tool(description = """
            Creates a Trello card.
            listId and name are required by this wrapper.
            due should use a Trello-compatible datetime format, usually ISO-8601.
            position accepts values such as top, bottom or a numeric string.
            Optional string fields on creation are omitted when null, empty or blank.
            """)
    public String crearTarjetaTrello(String listId, String name, String description, String due, String position)
            throws Exception {
        return objectMapper.writeValueAsString(
                trelloService.createCard(new CreateTrelloCardRequest(listId, name, description, due, position))
        );
    }

    @Tool(description = """
            Partially updates a Trello card.
            Null means "leave unchanged".
            For name, description and due, an empty string is forwarded to Trello and is not a no-op.
            dueComplete marks the due date as complete/incomplete without archiving the card.
            closed archives the card and is different from dueComplete.
            If the intent is to move the card to another list, prefer moverTarjetaTrello instead of using listId here.
            """)
    public String actualizarTarjetaTrello(
            String cardId,
            String name,
            String description,
            String due,
            Boolean dueComplete,
            String position,
            String listId,
            Boolean closed
    ) throws Exception {
        return objectMapper.writeValueAsString(
                trelloService.updateCard(cardId, new UpdateTrelloCardRequest(name, description, due, dueComplete, position, listId, closed))
        );
    }

    @Tool(description = "Deletes a Trello card from its cardId. This is a destructive operation.")
    public String eliminarTarjetaTrello(String cardId) throws Exception {
        return objectMapper.writeValueAsString(trelloService.deleteCard(cardId));
    }

    @Tool(description = """
            Moves a Trello card to another list.
            listId is required and must be the destination list identifier.
            position accepts values such as top, bottom or a numeric string.
            Moving a card to a done/final list does not archive it and does not mark dueComplete automatically.
            """)
    public String moverTarjetaTrello(String cardId, String listId, String position) throws Exception {
        return objectMapper.writeValueAsString(
                trelloService.moveCard(cardId, new MoveTrelloCardRequest(listId, position))
        );
    }

    @Tool(description = "Lists the checklists and checkItems of a Trello card, including each item state.")
    public String listarChecklistsTarjetaTrello(String cardId) throws Exception {
        return objectMapper.writeValueAsString(trelloService.listChecklists(cardId));
    }

    @Tool(description = """
            Creates a checklist on a Trello card.
            name is required by this wrapper.
            position accepts values such as top, bottom or a numeric string.
            sourceChecklistId optionally clones an existing checklist.
            """)
    public String crearChecklistTrello(String cardId, String name, String position, String sourceChecklistId)
            throws Exception {
        return objectMapper.writeValueAsString(
                trelloService.createChecklist(cardId, new CreateTrelloChecklistRequest(name, position, sourceChecklistId))
        );
    }

    @Tool(description = """
            Partially updates a Trello checklist.
            Null means "leave unchanged".
            For name, an empty string is forwarded to Trello and is not a no-op.
            Blank position values are ignored by this wrapper.
            """)
    public String actualizarChecklistTrello(String checklistId, String name, String position) throws Exception {
        return objectMapper.writeValueAsString(
                trelloService.updateChecklist(checklistId, new UpdateTrelloChecklistRequest(name, position))
        );
    }

    @Tool(description = "Deletes a Trello checklist from its checklistId. This is a destructive operation.")
    public String eliminarChecklistTrello(String checklistId) throws Exception {
        return objectMapper.writeValueAsString(trelloService.deleteChecklist(checklistId));
    }

    @Tool(description = """
            Creates a check item inside a Trello checklist.
            name is required by this wrapper.
            checked=true creates the item as complete; checked=false creates it as incomplete.
            position accepts values such as top, bottom or a numeric string.
            """)
    public String crearItemChecklistTrello(String checklistId, String name, Boolean checked, String position)
            throws Exception {
        return objectMapper.writeValueAsString(
                trelloService.createCheckItem(checklistId, new CreateTrelloCheckItemRequest(name, checked, position))
        );
    }

    @Tool(description = """
            Partially updates a checklist item in a Trello card.
            Null means "leave unchanged".
            checked=true maps to complete and checked=false maps to incomplete.
            checklistId optionally moves the item to another checklist.
            For name, an empty string is forwarded to Trello and is not a no-op.
            Blank position and checklistId values are ignored by this wrapper.
            """)
    public String actualizarItemChecklistTrello(
            String cardId,
            String checkItemId,
            String name,
            Boolean checked,
            String position,
            String checklistId
    ) throws Exception {
        return objectMapper.writeValueAsString(
                trelloService.updateCheckItem(
                        cardId,
                        checkItemId,
                        new UpdateTrelloCheckItemRequest(name, checked, position, checklistId)
                )
        );
    }

    @Tool(description = "Deletes a checklist item from a Trello card. This is a destructive operation.")
    public String eliminarItemChecklistTrello(String cardId, String checkItemId) throws Exception {
        return objectMapper.writeValueAsString(trelloService.deleteCheckItem(cardId, checkItemId));
    }
}
