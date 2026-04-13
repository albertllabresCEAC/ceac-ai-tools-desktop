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
import tools.ceac.ai.modules.trello.domain.model.CreateTrelloChecklistRequest;
import tools.ceac.ai.modules.trello.domain.model.TrelloChecklistSummary;
import tools.ceac.ai.modules.trello.domain.model.TrelloOperationResult;
import tools.ceac.ai.modules.trello.domain.model.UpdateTrelloChecklistRequest;

@RestController
@RequestMapping("/api/trello")
@Tag(name = "Trello Checklists", description = "Consulta y gestion de checklists asociadas a tarjetas")
public class TrelloChecklistController {

    private final TrelloService trelloService;

    public TrelloChecklistController(TrelloService trelloService) {
        this.trelloService = trelloService;
    }

    @Operation(summary = "List card checklists", description = "Returns the checklists of a card together with their check items and item state.")
    @GetMapping("/cards/{cardId}/checklists")
    public List<TrelloChecklistSummary> checklists(@PathVariable String cardId) {
        return trelloService.listChecklists(cardId);
    }

    @Operation(summary = "Create checklist", description = "Creates a checklist inside a card.")
    @PostMapping("/cards/{cardId}/checklists")
    public TrelloChecklistSummary createChecklist(@PathVariable String cardId,
                                                  @Valid @RequestBody CreateTrelloChecklistRequest request) {
        return trelloService.createChecklist(cardId, request);
    }

    @Operation(summary = "Update checklist", description = "Partially updates a checklist. Null fields are ignored. Empty strings for name are forwarded to Trello.")
    @PutMapping("/checklists/{checklistId}")
    public TrelloChecklistSummary updateChecklist(@PathVariable String checklistId,
                                                  @RequestBody UpdateTrelloChecklistRequest request) {
        return trelloService.updateChecklist(checklistId, request);
    }

    @Operation(summary = "Delete checklist", description = "Deletes a checklist. This operation is destructive.")
    @DeleteMapping("/checklists/{checklistId}")
    public TrelloOperationResult deleteChecklist(@PathVariable String checklistId) {
        return trelloService.deleteChecklist(checklistId);
    }
}
