package tools.ceac.ai.modules.trello.interfaces.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.ceac.ai.modules.trello.application.service.TrelloService;
import tools.ceac.ai.modules.trello.domain.model.CreateCustomFieldOptionRequest;
import tools.ceac.ai.modules.trello.domain.model.CreateCustomFieldRequest;
import tools.ceac.ai.modules.trello.domain.model.TrelloCustomFieldDefinition;
import tools.ceac.ai.modules.trello.domain.model.TrelloCustomFieldItem;
import tools.ceac.ai.modules.trello.domain.model.TrelloCustomFieldOption;
import tools.ceac.ai.modules.trello.domain.model.TrelloOperationResult;
import tools.ceac.ai.modules.trello.domain.model.UpdateCustomFieldItemRequest;
import tools.ceac.ai.modules.trello.domain.model.UpdateCustomFieldRequest;

@RestController
@RequestMapping("/api/trello")
@Tag(name = "Trello Custom Fields", description = "Gestión de campos personalizados en tableros y valores en tarjetas")
public class TrelloCustomFieldController {

    private final TrelloService trelloService;

    public TrelloCustomFieldController(TrelloService trelloService) {
        this.trelloService = trelloService;
    }

    // ── Definiciones de campo ─────────────────────────────────────────────────

    @Operation(summary = "List custom fields",
            description = "Returns all custom field definitions for a board.")
    @GetMapping("/boards/{boardId}/custom-fields")
    public List<TrelloCustomFieldDefinition> listCustomFields(@PathVariable String boardId) {
        return trelloService.listCustomFields(boardId);
    }

    @Operation(summary = "Create custom field",
            description = "Creates a new custom field on a board. boardId, name and type are required. "
                    + "Supported types: text, number, date, checkbox, list.")
    @PostMapping("/custom-fields")
    public TrelloCustomFieldDefinition createCustomField(@Valid @RequestBody CreateCustomFieldRequest request) {
        return trelloService.createCustomField(request);
    }

    @Operation(summary = "Update custom field",
            description = "Partially updates a custom field definition. Null fields are ignored.")
    @PutMapping("/custom-fields/{customFieldId}")
    public TrelloCustomFieldDefinition updateCustomField(
            @PathVariable String customFieldId,
            @RequestBody UpdateCustomFieldRequest request) {
        return trelloService.updateCustomField(customFieldId, request);
    }

    @Operation(summary = "Delete custom field",
            description = "Permanently deletes a custom field definition and all its values from every card in the board.")
    @DeleteMapping("/custom-fields/{customFieldId}")
    public TrelloOperationResult deleteCustomField(@PathVariable String customFieldId) {
        return trelloService.deleteCustomField(customFieldId);
    }

    // ── Opciones de campo tipo lista ──────────────────────────────────────────

    @Operation(summary = "Create custom field option",
            description = "Adds a new option to a list-type custom field. text is required.")
    @PostMapping("/custom-fields/{customFieldId}/options")
    public TrelloCustomFieldOption createCustomFieldOption(
            @PathVariable String customFieldId,
            @Valid @RequestBody CreateCustomFieldOptionRequest request) {
        return trelloService.createCustomFieldOption(customFieldId, request);
    }

    @Operation(summary = "Delete custom field option",
            description = "Removes an option from a list-type custom field.")
    @DeleteMapping("/custom-fields/{customFieldId}/options/{optionId}")
    public TrelloOperationResult deleteCustomFieldOption(
            @PathVariable String customFieldId,
            @PathVariable String optionId) {
        return trelloService.deleteCustomFieldOption(customFieldId, optionId);
    }

    // ── Valores en tarjetas ───────────────────────────────────────────────────

    @Operation(summary = "List card custom field items",
            description = "Returns the current custom field values for a card. "
                    + "Fields with no value set on the card are not included in the response.")
    @GetMapping("/cards/{cardId}/custom-field-items")
    public List<TrelloCustomFieldItem> listCardCustomFieldItems(@PathVariable String cardId) {
        return trelloService.listCardCustomFieldItems(cardId);
    }

    @Operation(summary = "Set card custom field value",
            description = "Sets or clears a custom field value on a card. "
                    + "Provide the typed value matching the field type, or set clear=true to remove it. "
                    + "For list fields use idValue (option id); pass an empty string to clear the selection.")
    @PutMapping("/cards/{cardId}/custom-fields/{customFieldId}")
    public TrelloCustomFieldItem updateCardCustomFieldItem(
            @PathVariable String cardId,
            @PathVariable String customFieldId,
            @RequestBody UpdateCustomFieldItemRequest request) {
        return trelloService.updateCardCustomFieldItem(cardId, customFieldId, request);
    }
}
