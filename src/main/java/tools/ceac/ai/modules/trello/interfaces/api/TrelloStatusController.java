package tools.ceac.ai.modules.trello.interfaces.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.ceac.ai.modules.trello.application.service.TrelloService;
import tools.ceac.ai.modules.trello.domain.model.TrelloMemberProfile;
import tools.ceac.ai.modules.trello.domain.model.TrelloStatusResponse;

@RestController
@RequestMapping("/api/trello")
@Tag(name = "Trello Status", description = "Estado de la conexion Trello y perfil de la cuenta conectada")
public class TrelloStatusController {

    private final TrelloService trelloService;

    public TrelloStatusController(TrelloService trelloService) {
        this.trelloService = trelloService;
    }

    @Operation(summary = "Get wrapper status", description = "Returns whether the local Trello runtime has a configured account and basic profile metadata.")
    @GetMapping("/status")
    public TrelloStatusResponse status() {
        return trelloService.status();
    }

    @Operation(summary = "Get current member", description = "Returns the Trello member profile associated with the current local token.")
    @GetMapping("/me")
    public TrelloMemberProfile me() {
        return trelloService.getCurrentMember();
    }
}
