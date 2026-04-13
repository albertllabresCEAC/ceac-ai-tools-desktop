package tools.ceac.ai.modules.trello.interfaces.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.ceac.ai.modules.trello.application.service.TrelloService;
import tools.ceac.ai.modules.trello.domain.model.TrelloCardSummary;

@RestController
@RequestMapping("/api/trello")
@Tag(name = "Trello Lists", description = "Consulta de listas y tarjetas contenidas en cada lista")
public class TrelloListController {

    private final TrelloService trelloService;

    public TrelloListController(TrelloService trelloService) {
        this.trelloService = trelloService;
    }

    @Operation(summary = "List list cards", description = "Returns the cards of a given list.")
    @GetMapping("/lists/{listId}/cards")
    public List<TrelloCardSummary> cards(@PathVariable String listId) {
        return trelloService.listCards(listId);
    }
}
