package tools.ceac.ai.modules.trello.interfaces.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import tools.ceac.ai.modules.trello.application.service.TrelloService;
import tools.ceac.ai.modules.trello.domain.model.CreateCustomFieldOptionRequest;
import tools.ceac.ai.modules.trello.domain.model.CreateCustomFieldRequest;
import tools.ceac.ai.modules.trello.domain.model.CreateTrelloBoardRequest;
import tools.ceac.ai.modules.trello.domain.model.CreateTrelloCheckItemRequest;
import tools.ceac.ai.modules.trello.domain.model.CreateTrelloChecklistRequest;
import tools.ceac.ai.modules.trello.domain.model.CreateTrelloCardRequest;
import tools.ceac.ai.modules.trello.domain.model.CreateTrelloListRequest;
import tools.ceac.ai.modules.trello.domain.model.MoveTrelloCardRequest;
import tools.ceac.ai.modules.trello.domain.model.UpdateCustomFieldItemRequest;
import tools.ceac.ai.modules.trello.domain.model.UpdateCustomFieldRequest;
import tools.ceac.ai.modules.trello.domain.model.UpdateTrelloBoardRequest;
import tools.ceac.ai.modules.trello.domain.model.UpdateTrelloCardRequest;
import tools.ceac.ai.modules.trello.domain.model.UpdateTrelloCheckItemRequest;
import tools.ceac.ai.modules.trello.domain.model.UpdateTrelloChecklistRequest;
import tools.ceac.ai.modules.trello.domain.model.UpdateTrelloListRequest;

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

    @Tool(description = "Returns the detail of a single Trello board from its boardId.")
    public String verTableroTrello(String boardId) throws Exception {
        return objectMapper.writeValueAsString(trelloService.getBoard(boardId));
    }

    @Tool(description = """
            Creates a new Trello board.
            name is required.
            defaultLists=true pre-populates the board with To Do, Doing and Done lists; false creates an empty board.
            """)
    public String crearTableroTrello(String name, String desc, Boolean defaultLists) throws Exception {
        return objectMapper.writeValueAsString(
                trelloService.createBoard(new CreateTrelloBoardRequest(name, desc, defaultLists))
        );
    }

    @Tool(description = """
            Partially updates a Trello board.
            Null means "leave unchanged".
            For name and desc, an empty string is forwarded to Trello and is not a no-op.
            closed=true archives the board; closed=false unarchives it.
            """)
    public String actualizarTableroTrello(String boardId, String name, String desc, Boolean closed) throws Exception {
        return objectMapper.writeValueAsString(
                trelloService.updateBoard(boardId, new UpdateTrelloBoardRequest(name, desc, closed))
        );
    }

    @Tool(description = """
            Archives (closes) a Trello board from its boardId.
            Trello does not support permanent deletion of boards via API.
            """)
    public String cerrarTableroTrello(String boardId) throws Exception {
        return objectMapper.writeValueAsString(trelloService.closeBoard(boardId));
    }

    @Tool(description = "Lists the lists of a Trello board from its boardId.")
    public String listarListasTrello(String boardId) throws Exception {
        return objectMapper.writeValueAsString(trelloService.listLists(boardId));
    }

    @Tool(description = "Returns the detail of a single Trello list from its listId.")
    public String verListaTrello(String listId) throws Exception {
        return objectMapper.writeValueAsString(trelloService.getList(listId));
    }

    @Tool(description = """
            Creates a new list inside a Trello board.
            boardId and name are required.
            position accepts values such as top, bottom or a numeric string.
            """)
    public String crearListaTrello(String boardId, String name, String position) throws Exception {
        return objectMapper.writeValueAsString(
                trelloService.createList(new CreateTrelloListRequest(boardId, name, position))
        );
    }

    @Tool(description = """
            Partially updates a Trello list.
            Null means "leave unchanged".
            For name, an empty string is forwarded to Trello and is not a no-op.
            closed=true archives the list; closed=false unarchives it.
            boardId moves the list to another board.
            Blank position and boardId values are ignored by this wrapper.
            """)
    public String actualizarListaTrello(String listId, String name, Boolean closed, String position, String boardId)
            throws Exception {
        return objectMapper.writeValueAsString(
                trelloService.updateList(listId, new UpdateTrelloListRequest(name, closed, position, boardId))
        );
    }

    @Tool(description = """
            Archives a Trello list from its listId.
            Trello does not support permanent deletion of lists via API.
            """)
    public String archivarListaTrello(String listId) throws Exception {
        return objectMapper.writeValueAsString(trelloService.archiveList(listId));
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

    // ── Custom Fields ────────────────────────────────────────────────────────

    @Tool(description = "Lists all custom field definitions on a Trello board.")
    public String listarCamposPersonalizadosTrello(String boardId) throws Exception {
        return objectMapper.writeValueAsString(trelloService.listCustomFields(boardId));
    }

    @Tool(description = """
            Creates a new custom field on a Trello board.
            boardId, name and type are required.
            Supported types: text, number, date, checkbox, list.
            displayCardFront=true shows the field on the card front (default).
            """)
    public String crearCampoPersonalizadoTrello(String boardId, String name, String type,
            String position, Boolean displayCardFront) throws Exception {
        return objectMapper.writeValueAsString(
                trelloService.createCustomField(new CreateCustomFieldRequest(boardId, name, type, position, displayCardFront))
        );
    }

    @Tool(description = """
            Partially updates a Trello custom field definition.
            Null means "leave unchanged".
            For name, an empty string is forwarded to Trello.
            Blank position values are ignored.
            """)
    public String actualizarCampoPersonalizadoTrello(String customFieldId, String name,
            String position, Boolean displayCardFront) throws Exception {
        return objectMapper.writeValueAsString(
                trelloService.updateCustomField(customFieldId, new UpdateCustomFieldRequest(name, position, displayCardFront))
        );
    }

    @Tool(description = "Permanently deletes a custom field definition and all its values from every card in the board. This is a destructive operation.")
    public String eliminarCampoPersonalizadoTrello(String customFieldId) throws Exception {
        return objectMapper.writeValueAsString(trelloService.deleteCustomField(customFieldId));
    }

    @Tool(description = """
            Adds an option to a list-type Trello custom field.
            text is required (the display label).
            Valid colors: none, black, blue, green, lime, orange, pink, purple, red, sky, yellow.
            position accepts values such as top, bottom or a numeric string.
            """)
    public String crearOpcionCampoPersonalizadoTrello(String customFieldId, String text,
            String color, String position) throws Exception {
        return objectMapper.writeValueAsString(
                trelloService.createCustomFieldOption(customFieldId, new CreateCustomFieldOptionRequest(text, color, position))
        );
    }

    @Tool(description = "Removes an option from a list-type Trello custom field. This is a destructive operation.")
    public String eliminarOpcionCampoPersonalizadoTrello(String customFieldId, String optionId) throws Exception {
        return objectMapper.writeValueAsString(trelloService.deleteCustomFieldOption(customFieldId, optionId));
    }

    @Tool(description = "Returns the current custom field values for a Trello card. Fields with no value set are not included.")
    public String listarValoresCamposPersonalizadosTarjetaTrello(String cardId) throws Exception {
        return objectMapper.writeValueAsString(trelloService.listCardCustomFieldItems(cardId));
    }

    @Tool(description = """
            Sets or clears a custom field value on a Trello card.
            Provide exactly one value matching the field type:
              text       → for text fields
              number     → for number fields (as a string)
              date       → for date fields (ISO-8601 datetime)
              checked    → for checkbox fields (true/false)
              idValue    → for list fields (option id); pass empty string to clear the selection
            Set clear=true to remove the value regardless of type.
            """)
    public String actualizarValorCampoPersonalizadoTarjetaTrello(
            String cardId,
            String customFieldId,
            String text,
            String number,
            String date,
            Boolean checked,
            String idValue,
            boolean clear
    ) throws Exception {
        return objectMapper.writeValueAsString(
                trelloService.updateCardCustomFieldItem(
                        cardId,
                        customFieldId,
                        new UpdateCustomFieldItemRequest(text, number, date, checked, idValue, clear)
                )
        );
    }
}
