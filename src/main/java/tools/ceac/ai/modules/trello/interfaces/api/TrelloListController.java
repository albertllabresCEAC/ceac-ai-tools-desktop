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
import tools.ceac.ai.modules.trello.domain.model.CreateTrelloListRequest;
import tools.ceac.ai.modules.trello.domain.model.TrelloCardSummary;
import tools.ceac.ai.modules.trello.domain.model.TrelloListSummary;
import tools.ceac.ai.modules.trello.domain.model.TrelloOperationResult;
import tools.ceac.ai.modules.trello.domain.model.UpdateTrelloListRequest;

@RestController
@RequestMapping("/api/trello")
@Tag(name = "Trello Lists", description = "CRUD de listas y consulta de tarjetas contenidas en cada lista")
public class TrelloListController {

    private final TrelloService trelloService;

    public TrelloListController(TrelloService trelloService) {
        this.trelloService = trelloService;
    }

    @Operation(summary = "Get list", description = "Returns the detail of a single list.")
    @GetMapping("/lists/{listId}")
    public TrelloListSummary list(@PathVariable String listId) {
        return trelloService.getList(listId);
    }

    @Operation(summary = "Create list", description = "Creates a new list inside a board. boardId and name are required.")
    @PostMapping("/lists")
    public TrelloListSummary createList(@Valid @RequestBody CreateTrelloListRequest request) {
        return trelloService.createList(request);
    }

    @Operation(summary = "Update list",
            description = "Partially updates a list. Null fields are ignored. closed=true archives the list. boardId moves the list to another board.")
    @PutMapping("/lists/{listId}")
    public TrelloListSummary updateList(@PathVariable String listId, @RequestBody UpdateTrelloListRequest request) {
        return trelloService.updateList(listId, request);
    }

    @Operation(summary = "Archive list",
            description = "Archives a Trello list. Trello does not support permanent deletion of lists via API.")
    @DeleteMapping("/lists/{listId}")
    public TrelloOperationResult archiveList(@PathVariable String listId) {
        return trelloService.archiveList(listId);
    }

    @Operation(summary = "List list cards", description = "Returns the cards of a given list.")
    @GetMapping("/lists/{listId}/cards")
    public List<TrelloCardSummary> cards(@PathVariable String listId) {
        return trelloService.listCards(listId);
    }
}
