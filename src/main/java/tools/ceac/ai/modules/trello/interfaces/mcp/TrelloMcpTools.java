package tools.ceac.ai.modules.trello.interfaces.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import tools.ceac.ai.modules.trello.application.service.TrelloService;
import tools.ceac.ai.modules.trello.domain.model.CreateTrelloCardRequest;

@Component
public class TrelloMcpTools {

    private final TrelloService trelloService;
    private final ObjectMapper objectMapper;

    public TrelloMcpTools(TrelloService trelloService, ObjectMapper objectMapper) {
        this.trelloService = trelloService;
        this.objectMapper = objectMapper;
    }

    @Tool(description = "Devuelve el estado actual de la conexion Trello usada por el runtime local.")
    public String verEstadoTrello() throws Exception {
        return objectMapper.writeValueAsString(trelloService.status());
    }

    @Tool(description = "Lista los tableros visibles para la cuenta Trello conectada.")
    public String listarTablerosTrello() throws Exception {
        return objectMapper.writeValueAsString(trelloService.listBoards());
    }

    @Tool(description = "Lista las listas de un tablero de Trello a partir de su boardId.")
    public String listarListasTrello(String boardId) throws Exception {
        return objectMapper.writeValueAsString(trelloService.listLists(boardId));
    }

    @Tool(description = "Lista las tarjetas de una lista de Trello a partir de su listId.")
    public String listarTarjetasTrello(String listId) throws Exception {
        return objectMapper.writeValueAsString(trelloService.listCards(listId));
    }

    @Tool(description = """
            Crea una tarjeta en Trello.
            listId es obligatorio y debe ser el identificador de la lista de destino.
            name es el titulo de la tarjeta.
            description, due y position son opcionales.
            """)
    public String crearTarjetaTrello(String listId, String name, String description, String due, String position)
            throws Exception {
        return objectMapper.writeValueAsString(
                trelloService.createCard(new CreateTrelloCardRequest(listId, name, description, due, position))
        );
    }
}
