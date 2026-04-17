package tools.ceac.ai.modules.trello.interfaces.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.ceac.ai.modules.trello.application.service.TrelloService;
import tools.ceac.ai.modules.trello.domain.model.CreateTrelloCardRequest;
import tools.ceac.ai.modules.trello.domain.model.MoveTrelloCardRequest;
import tools.ceac.ai.modules.trello.domain.model.TrelloCardSummary;
import tools.ceac.ai.modules.trello.domain.model.TrelloOperationResult;
import tools.ceac.ai.modules.trello.domain.model.UpdateTrelloCardRequest;

@RestController
@RequestMapping("/api/trello")
@Tag(name = "Trello Cards", description = "CRUD y movimiento de tarjetas")
public class TrelloCardController {

    private final TrelloService trelloService;

    public TrelloCardController(TrelloService trelloService) {
        this.trelloService = trelloService;
    }

    /**
     * Returns a single Trello card including current custom field values when present.
     */
    @Operation(summary = "Get card",
            description = "Returns the current detail of a Trello card, including the current custom field values when present.")
    @GetMapping("/cards/{cardId}")
    public TrelloCardSummary card(@PathVariable String cardId) {
        return trelloService.getCard(cardId);
    }

    @Operation(summary = "Create card", description = "Creates a card in a destination list.")
    @PostMapping("/cards")
    public TrelloCardSummary createCard(@Valid @RequestBody CreateTrelloCardRequest request) {
        return trelloService.createCard(request);
    }

    @Operation(
            summary = "Update card",
            description = "Partially updates a card. Null fields are ignored. Empty strings for name, description and due are forwarded to Trello and are not no-ops. closed archives the card; dueComplete only marks the due date as complete."
    )
    @PutMapping("/cards/{cardId}")
    public TrelloCardSummary updateCard(@PathVariable String cardId, @RequestBody UpdateTrelloCardRequest request) {
        return trelloService.updateCard(cardId, request);
    }

    @Operation(summary = "Delete card", description = "Deletes a Trello card. This operation is destructive.")
    @DeleteMapping("/cards/{cardId}")
    public TrelloOperationResult deleteCard(@PathVariable String cardId) {
        return trelloService.deleteCard(cardId);
    }

    @Operation(summary = "Move card", description = "Moves a card to another list. This does not archive the card and does not mark the due date as complete.")
    @PutMapping("/cards/{cardId}/move")
    public TrelloCardSummary moveCard(@PathVariable String cardId, @Valid @RequestBody MoveTrelloCardRequest request) {
        return trelloService.moveCard(cardId, request);
    }
}
