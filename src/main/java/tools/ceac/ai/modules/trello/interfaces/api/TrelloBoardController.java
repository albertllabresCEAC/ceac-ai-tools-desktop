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
import tools.ceac.ai.modules.trello.domain.model.CreateTrelloBoardRequest;
import tools.ceac.ai.modules.trello.domain.model.TrelloBoardSummary;
import tools.ceac.ai.modules.trello.domain.model.TrelloListSummary;
import tools.ceac.ai.modules.trello.domain.model.TrelloOperationResult;
import tools.ceac.ai.modules.trello.domain.model.UpdateTrelloBoardRequest;

@RestController
@RequestMapping("/api/trello")
@Tag(name = "Trello Boards", description = "CRUD de tableros y consulta de listas asociadas")
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

    @Operation(summary = "Get board", description = "Returns the detail of a single board.")
    @GetMapping("/boards/{boardId}")
    public TrelloBoardSummary board(@PathVariable String boardId) {
        return trelloService.getBoard(boardId);
    }

    @Operation(summary = "Create board", description = "Creates a new Trello board. name is required.")
    @PostMapping("/boards")
    public TrelloBoardSummary createBoard(@Valid @RequestBody CreateTrelloBoardRequest request) {
        return trelloService.createBoard(request);
    }

    @Operation(summary = "Update board",
            description = "Partially updates a board. Null fields are ignored. closed=true archives the board.")
    @PutMapping("/boards/{boardId}")
    public TrelloBoardSummary updateBoard(@PathVariable String boardId, @RequestBody UpdateTrelloBoardRequest request) {
        return trelloService.updateBoard(boardId, request);
    }

    @Operation(summary = "Close board",
            description = "Archives (closes) a Trello board. Trello does not support permanent deletion via API.")
    @DeleteMapping("/boards/{boardId}")
    public TrelloOperationResult closeBoard(@PathVariable String boardId) {
        return trelloService.closeBoard(boardId);
    }

    @Operation(summary = "List board lists", description = "Returns the lists of a given board.")
    @GetMapping("/boards/{boardId}/lists")
    public List<TrelloListSummary> lists(@PathVariable String boardId) {
        return trelloService.listLists(boardId);
    }
}
