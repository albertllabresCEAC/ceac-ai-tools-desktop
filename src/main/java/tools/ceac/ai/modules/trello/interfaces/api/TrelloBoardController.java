package tools.ceac.ai.modules.trello.interfaces.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.ceac.ai.modules.trello.application.service.TrelloService;
import tools.ceac.ai.modules.trello.domain.model.TrelloBoardSummary;
import tools.ceac.ai.modules.trello.domain.model.TrelloListSummary;

@RestController
@RequestMapping("/api/trello")
@Tag(name = "Trello Boards", description = "Consulta de tableros y listas asociadas")
public class TrelloBoardController {

    private final TrelloService trelloService;

    public TrelloBoardController(TrelloService trelloService) {
        this.trelloService = trelloService;
    }

    @Operation(summary = "List boards", description = "Returns the boards visible to the connected Trello account.")
    @GetMapping("/boards")
    public List<TrelloBoardSummary> boards() {
        return trelloService.listBoards();
    }

    @Operation(summary = "List board lists", description = "Returns the lists of a given board.")
    @GetMapping("/boards/{boardId}/lists")
    public List<TrelloListSummary> lists(@PathVariable String boardId) {
        return trelloService.listLists(boardId);
    }
}
