package tools.ceac.ai.modules.trello.interfaces.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.ceac.ai.modules.trello.application.service.TrelloService;
import tools.ceac.ai.modules.trello.domain.model.CreateTrelloCheckItemRequest;
import tools.ceac.ai.modules.trello.domain.model.TrelloCheckItemSummary;
import tools.ceac.ai.modules.trello.domain.model.TrelloOperationResult;
import tools.ceac.ai.modules.trello.domain.model.UpdateTrelloCheckItemRequest;

@RestController
@RequestMapping("/api/trello")
@Tag(name = "Trello CheckItems", description = "Gestion de items de checklist y su estado")
public class TrelloCheckItemController {

    private final TrelloService trelloService;

    public TrelloCheckItemController(TrelloService trelloService) {
        this.trelloService = trelloService;
    }

    @Operation(summary = "Create checklist item", description = "Creates an item inside a checklist.")
    @PostMapping("/checklists/{checklistId}/items")
    public TrelloCheckItemSummary createCheckItem(@PathVariable String checklistId,
                                                  @Valid @RequestBody CreateTrelloCheckItemRequest request) {
        return trelloService.createCheckItem(checklistId, request);
    }

    @Operation(summary = "Update checklist item", description = "Partially updates a checklist item. checked=true maps to complete, checked=false maps to incomplete. checklistId can move the item to another checklist.")
    @PutMapping("/cards/{cardId}/checkitems/{checkItemId}")
    public TrelloCheckItemSummary updateCheckItem(@PathVariable String cardId,
                                                  @PathVariable String checkItemId,
                                                  @RequestBody UpdateTrelloCheckItemRequest request) {
        return trelloService.updateCheckItem(cardId, checkItemId, request);
    }

    @Operation(summary = "Delete checklist item", description = "Deletes a checklist item. This operation is destructive.")
    @DeleteMapping("/cards/{cardId}/checkitems/{checkItemId}")
    public TrelloOperationResult deleteCheckItem(@PathVariable String cardId, @PathVariable String checkItemId) {
        return trelloService.deleteCheckItem(cardId, checkItemId);
    }
}
