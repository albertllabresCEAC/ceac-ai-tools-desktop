package tools.ceac.ai.modules.trello.interfaces.api;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.ceac.ai.modules.trello.application.service.TrelloService;
import tools.ceac.ai.modules.trello.domain.model.CreateTrelloCardRequest;
import tools.ceac.ai.modules.trello.domain.model.TrelloBoardSummary;
import tools.ceac.ai.modules.trello.domain.model.TrelloCardSummary;
import tools.ceac.ai.modules.trello.domain.model.TrelloListSummary;
import tools.ceac.ai.modules.trello.domain.model.TrelloMemberProfile;
import tools.ceac.ai.modules.trello.domain.model.TrelloStatusResponse;

@Validated
@RestController
@RequestMapping("/api/trello")
public class TrelloController {

    private final TrelloService trelloService;

    public TrelloController(TrelloService trelloService) {
        this.trelloService = trelloService;
    }

    @GetMapping("/status")
    public TrelloStatusResponse status() {
        return trelloService.status();
    }

    @GetMapping("/me")
    public TrelloMemberProfile me() {
        return trelloService.getCurrentMember();
    }

    @GetMapping("/boards")
    public List<TrelloBoardSummary> boards() {
        return trelloService.listBoards();
    }

    @GetMapping("/boards/{boardId}/lists")
    public List<TrelloListSummary> lists(@PathVariable String boardId) {
        return trelloService.listLists(boardId);
    }

    @GetMapping("/lists/{listId}/cards")
    public List<TrelloCardSummary> cards(@PathVariable String listId) {
        return trelloService.listCards(listId);
    }

    @PostMapping("/cards")
    public TrelloCardSummary createCard(@Valid @RequestBody CreateTrelloCardRequest request) {
        return trelloService.createCard(request);
    }
}
