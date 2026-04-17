package tools.ceac.ai.modules.trello.infrastructure.trello;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.ceac.ai.modules.trello.domain.model.CreateCustomFieldOptionRequest;
import tools.ceac.ai.modules.trello.domain.model.CreateCustomFieldRequest;
import tools.ceac.ai.modules.trello.domain.model.CreateTrelloBoardRequest;
import tools.ceac.ai.modules.trello.domain.model.CreateTrelloCheckItemRequest;
import tools.ceac.ai.modules.trello.domain.model.CreateTrelloChecklistRequest;
import tools.ceac.ai.modules.trello.application.auth.TrelloRuntimeCredentials;
import tools.ceac.ai.modules.trello.domain.model.CreateTrelloCardRequest;
import tools.ceac.ai.modules.trello.domain.model.CreateTrelloListRequest;
import tools.ceac.ai.modules.trello.domain.model.TrelloBoardSummary;
import tools.ceac.ai.modules.trello.domain.model.TrelloCardSummary;
import tools.ceac.ai.modules.trello.domain.model.TrelloCheckItemSummary;
import tools.ceac.ai.modules.trello.domain.model.TrelloChecklistSummary;
import tools.ceac.ai.modules.trello.domain.model.TrelloCustomFieldDefinition;
import tools.ceac.ai.modules.trello.domain.model.TrelloCustomFieldItem;
import tools.ceac.ai.modules.trello.domain.model.TrelloCustomFieldOption;
import tools.ceac.ai.modules.trello.domain.model.TrelloListSummary;
import tools.ceac.ai.modules.trello.domain.model.TrelloMemberProfile;
import tools.ceac.ai.modules.trello.domain.model.TrelloOperationResult;
import tools.ceac.ai.modules.trello.domain.model.UpdateCustomFieldItemRequest;
import tools.ceac.ai.modules.trello.domain.model.UpdateCustomFieldRequest;
import tools.ceac.ai.modules.trello.domain.model.UpdateTrelloBoardRequest;
import tools.ceac.ai.modules.trello.domain.model.UpdateTrelloCardRequest;
import tools.ceac.ai.modules.trello.domain.model.UpdateTrelloCheckItemRequest;
import tools.ceac.ai.modules.trello.domain.model.UpdateTrelloChecklistRequest;
import tools.ceac.ai.modules.trello.domain.model.UpdateTrelloListRequest;

@Component
public class TrelloHttpClient {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private final ObjectMapper objectMapper;
    private final TrelloRuntimeCredentials credentials;

    public TrelloHttpClient(ObjectMapper objectMapper, TrelloRuntimeCredentials credentials) {
        this.objectMapper = objectMapper;
        this.credentials = credentials;
    }

    public TrelloMemberProfile getCurrentMember() {
        return readObject(
                "/members/me",
                Map.of("fields", "id,username,fullName,initials,url"),
                TrelloMemberProfile.class
        );
    }

    public List<TrelloBoardSummary> listBoards() {
        return readList(
                "/members/me/boards",
                Map.of("fields", "id,name,desc,url,closed"),
                new TypeReference<>() {
                }
        );
    }

    public TrelloBoardSummary getBoard(String boardId) {
        return readObject(
                "/boards/" + encodePath(boardId),
                Map.of("fields", "id,name,desc,url,closed"),
                TrelloBoardSummary.class
        );
    }

    public TrelloBoardSummary createBoard(CreateTrelloBoardRequest request) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("name", request.name());
        if (request.desc() != null && !request.desc().isBlank()) {
            params.put("desc", request.desc());
        }
        if (request.defaultLists() != null) {
            params.put("defaultLists", request.defaultLists().toString());
        }
        return sendObject("POST", "/boards", params, TrelloBoardSummary.class);
    }

    public TrelloBoardSummary updateBoard(String boardId, UpdateTrelloBoardRequest request) {
        Map<String, String> params = new LinkedHashMap<>();
        putIfDefined(params, "name", request.name());
        putIfDefined(params, "desc", request.desc());
        if (request.closed() != null) {
            params.put("closed", request.closed().toString());
        }
        return sendObject("PUT", "/boards/" + encodePath(boardId), params, TrelloBoardSummary.class);
    }

    public TrelloOperationResult closeBoard(String boardId) {
        sendObject("PUT", "/boards/" + encodePath(boardId), Map.of("closed", "true"), TrelloBoardSummary.class);
        return new TrelloOperationResult(true, "close", "board", boardId, "Tablero cerrado.");
    }

    public List<TrelloListSummary> listLists(String boardId) {
        return readList(
                "/boards/" + encodePath(boardId) + "/lists",
                Map.of("fields", "id,idBoard,name,closed,pos"),
                new TypeReference<>() {
                }
        );
    }

    public TrelloListSummary getList(String listId) {
        return readObject(
                "/lists/" + encodePath(listId),
                Map.of("fields", "id,idBoard,name,closed,pos"),
                TrelloListSummary.class
        );
    }

    public TrelloListSummary createList(CreateTrelloListRequest request) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("idBoard", request.boardId());
        params.put("name", request.name());
        putIfNonBlank(params, "pos", request.position());
        return sendObject("POST", "/lists", params, TrelloListSummary.class);
    }

    public TrelloListSummary updateList(String listId, UpdateTrelloListRequest request) {
        Map<String, String> params = new LinkedHashMap<>();
        putIfDefined(params, "name", request.name());
        if (request.closed() != null) {
            params.put("closed", request.closed().toString());
        }
        putIfNonBlank(params, "pos", request.position());
        putIfNonBlank(params, "idBoard", request.boardId());
        return sendObject("PUT", "/lists/" + encodePath(listId), params, TrelloListSummary.class);
    }

    public TrelloOperationResult archiveList(String listId) {
        sendObject("PUT", "/lists/" + encodePath(listId), Map.of("closed", "true"), TrelloListSummary.class);
        return new TrelloOperationResult(true, "archive", "list", listId, "Lista archivada.");
    }

    public List<TrelloCardSummary> listCards(String listId) {
        return readList(
                "/lists/" + encodePath(listId) + "/cards",
                Map.of("fields", "id,idBoard,idList,name,desc,url,closed,due,dueComplete,pos,idMembers"),
                new TypeReference<>() {
                }
        );
    }

    public TrelloCardSummary createCard(CreateTrelloCardRequest request) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("idList", request.listId());
        params.put("name", request.name());
        if (request.description() != null && !request.description().isBlank()) {
            params.put("desc", request.description());
        }
        if (request.due() != null && !request.due().isBlank()) {
            params.put("due", request.due());
        }
        if (request.position() != null && !request.position().isBlank()) {
            params.put("pos", request.position());
        }
        return sendObject("POST", "/cards", params, TrelloCardSummary.class);
    }

    public TrelloCardSummary getCard(String cardId) {
        return readObject(
                "/cards/" + encodePath(cardId),
                Map.of("fields", "id,idBoard,idList,name,desc,url,closed,due,dueComplete,pos,idMembers"),
                TrelloCardSummary.class
        );
    }

    /**
     * Sends a partial card update to Trello.
     *
     * <p>Wrapper mapping rules:
     *
     * <ul>
     *   <li>Null fields are omitted from the outgoing Trello request.</li>
     *   <li>Blank values for {@code position} and {@code listId} are ignored.</li>
     *   <li>Empty strings for {@code name}, {@code description} and {@code due} are forwarded as-is.</li>
     * </ul>
     */
    public TrelloCardSummary updateCard(String cardId, UpdateTrelloCardRequest request) {
        Map<String, String> params = new LinkedHashMap<>();
        putIfDefined(params, "name", request.name());
        putIfDefined(params, "desc", request.description());
        putIfDefined(params, "due", request.due());
        if (request.dueComplete() != null) {
            params.put("dueComplete", request.dueComplete().toString());
        }
        putIfNonBlank(params, "pos", request.position());
        putIfNonBlank(params, "idList", request.listId());
        if (request.closed() != null) {
            params.put("closed", request.closed().toString());
        }
        return sendObject("PUT", "/cards/" + encodePath(cardId), params, TrelloCardSummary.class);
    }

    public TrelloOperationResult deleteCard(String cardId) {
        sendWithoutBody("DELETE", "/cards/" + encodePath(cardId), Map.of());
        return new TrelloOperationResult(true, "delete", "card", cardId, "Tarjeta eliminada.");
    }

    public List<TrelloChecklistSummary> listChecklists(String cardId) {
        return readList(
                "/cards/" + encodePath(cardId) + "/checklists",
                Map.of(
                        "fields", "id,idBoard,idCard,name,pos",
                        "checkItems", "all",
                        "checkItem_fields", "id,idChecklist,name,state,pos,due,dueReminder,idMember"
                ),
                new TypeReference<>() {
                }
        );
    }

    public TrelloChecklistSummary createChecklist(String cardId, CreateTrelloChecklistRequest request) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("name", request.name());
        putIfNonBlank(params, "pos", request.position());
        putIfNonBlank(params, "idChecklistSource", request.sourceChecklistId());
        return sendObject(
                "POST",
                "/cards/" + encodePath(cardId) + "/checklists",
                params,
                TrelloChecklistSummary.class
        );
    }

    public TrelloChecklistSummary updateChecklist(String checklistId, UpdateTrelloChecklistRequest request) {
        Map<String, String> params = new LinkedHashMap<>();
        putIfDefined(params, "name", request.name());
        putIfNonBlank(params, "pos", request.position());
        return sendObject("PUT", "/checklists/" + encodePath(checklistId), params, TrelloChecklistSummary.class);
    }

    public TrelloOperationResult deleteChecklist(String checklistId) {
        sendWithoutBody("DELETE", "/checklists/" + encodePath(checklistId), Map.of());
        return new TrelloOperationResult(true, "delete", "checklist", checklistId, "Checklist eliminada.");
    }

    public TrelloCheckItemSummary createCheckItem(String checklistId, CreateTrelloCheckItemRequest request) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("name", request.name());
        putIfNonBlank(params, "pos", request.position());
        if (request.checked() != null) {
            params.put("checked", request.checked().toString());
        }
        return sendObject(
                "POST",
                "/checklists/" + encodePath(checklistId) + "/checkItems",
                params,
                TrelloCheckItemSummary.class
        );
    }

    /**
     * Sends a partial checklist item update to Trello.
     *
     * <p>Blank values for {@code position} and {@code checklistId} are ignored by the wrapper.
     */
    public TrelloCheckItemSummary updateCheckItem(String cardId, String checkItemId, UpdateTrelloCheckItemRequest request) {
        Map<String, String> params = new LinkedHashMap<>();
        putIfDefined(params, "name", request.name());
        putIfNonBlank(params, "pos", request.position());
        putIfNonBlank(params, "idChecklist", request.checklistId());
        if (request.checked() != null) {
            params.put("state", request.checked() ? "complete" : "incomplete");
        }
        return sendObject(
                "PUT",
                "/cards/" + encodePath(cardId) + "/checkItem/" + encodePath(checkItemId),
                params,
                TrelloCheckItemSummary.class
        );
    }

    public TrelloOperationResult deleteCheckItem(String cardId, String checkItemId) {
        sendWithoutBody("DELETE", "/cards/" + encodePath(cardId) + "/checkItem/" + encodePath(checkItemId), Map.of());
        return new TrelloOperationResult(true, "delete", "checkItem", checkItemId, "Item de checklist eliminado.");
    }

    // ── Custom Fields ────────────────────────────────────────────────────────

    public List<TrelloCustomFieldDefinition> listCustomFields(String boardId) {
        return readList(
                "/boards/" + encodePath(boardId) + "/customFields",
                Map.of(),
                new TypeReference<>() {}
        );
    }

    public TrelloCustomFieldDefinition createCustomField(CreateCustomFieldRequest request) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("idModel", request.boardId());
        params.put("modelType", "board");
        params.put("name", request.name());
        params.put("type", request.type());
        putIfNonBlank(params, "pos", request.position());
        if (request.displayCardFront() != null) {
            params.put("display_cardFront", request.displayCardFront().toString());
        }
        return sendObject("POST", "/customFields", params, TrelloCustomFieldDefinition.class);
    }

    public TrelloCustomFieldDefinition updateCustomField(String customFieldId, UpdateCustomFieldRequest request) {
        Map<String, String> params = new LinkedHashMap<>();
        putIfDefined(params, "name", request.name());
        putIfNonBlank(params, "pos", request.position());
        if (request.displayCardFront() != null) {
            params.put("display/cardFront", request.displayCardFront().toString());
        }
        return sendObject("PUT", "/customFields/" + encodePath(customFieldId), params, TrelloCustomFieldDefinition.class);
    }

    public TrelloOperationResult deleteCustomField(String customFieldId) {
        sendWithoutBody("DELETE", "/customFields/" + encodePath(customFieldId), Map.of());
        return new TrelloOperationResult(true, "delete", "customField", customFieldId, "Campo personalizado eliminado.");
    }

    public TrelloCustomFieldOption createCustomFieldOption(String customFieldId, CreateCustomFieldOptionRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("value", Map.of("text", request.text()));
        if (request.color() != null && !request.color().isBlank()) {
            body.put("color", request.color());
        }
        if (request.position() != null && !request.position().isBlank()) {
            body.put("pos", request.position());
        }
        return sendJsonBody("POST", "/customFields/" + encodePath(customFieldId) + "/options", body, TrelloCustomFieldOption.class);
    }

    public TrelloOperationResult deleteCustomFieldOption(String customFieldId, String optionId) {
        sendWithoutBody("DELETE", "/customFields/" + encodePath(customFieldId) + "/options/" + encodePath(optionId), Map.of());
        return new TrelloOperationResult(true, "delete", "customFieldOption", optionId, "Opción de campo personalizado eliminada.");
    }

    public List<TrelloCustomFieldItem> listCardCustomFieldItems(String cardId) {
        return readList(
                "/cards/" + encodePath(cardId) + "/customFieldItems",
                Map.of(),
                new TypeReference<>() {}
        );
    }

    public TrelloCustomFieldItem updateCardCustomFieldItem(String cardId, String customFieldId,
            UpdateCustomFieldItemRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        if (request.clear()) {
            body.put("value", Map.of());
        } else if (request.idValue() != null) {
            body.put("idValue", request.idValue());
        } else {
            Map<String, String> value = new LinkedHashMap<>();
            if (request.text() != null) {
                value.put("text", request.text());
            }
            if (request.number() != null) {
                value.put("number", request.number());
            }
            if (request.date() != null) {
                value.put("date", request.date());
            }
            if (request.checked() != null) {
                value.put("checked", request.checked().toString());
            }
            body.put("value", value);
        }
        return sendJsonBody(
                "PUT",
                "/cards/" + encodePath(cardId) + "/customField/" + encodePath(customFieldId) + "/item",
                body,
                TrelloCustomFieldItem.class
        );
    }

    private <T> T readObject(String path, Map<String, String> params, Class<T> type) {
        return sendObject("GET", path, params, type);
    }

    private <T> T sendJsonBody(String method, String path, Object body, Class<T> type) {
        try {
            String json = objectMapper.writeValueAsString(body);
            credentials.assertConfigured();
            URI uri = URI.create(credentials.apiBaseUrl() + path + "?" + buildQuery(Map.of()));
            HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(30))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json");
            if ("PUT".equalsIgnoreCase(method)) {
                builder.PUT(HttpRequest.BodyPublishers.ofString(json));
            } else {
                builder.POST(HttpRequest.BodyPublishers.ofString(json));
            }
            HttpResponse<String> response = httpClient.send(
                    builder.build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );
            ensureSuccess(response);
            return objectMapper.readValue(response.body(), type);
        } catch (Exception exception) {
            throw new IllegalStateException("No he podido consultar Trello: " + exception.getMessage(), exception);
        }
    }

    private <T> List<T> readList(String path, Map<String, String> params, TypeReference<List<T>> type) {
        try {
            HttpResponse<String> response = httpClient.send(
                    request("GET", path, params),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );
            ensureSuccess(response);
            return objectMapper.readValue(response.body(), type);
        } catch (Exception exception) {
            throw new IllegalStateException("No he podido consultar Trello: " + exception.getMessage(), exception);
        }
    }

    private <T> T sendObject(String method, String path, Map<String, String> params, Class<T> type) {
        try {
            HttpResponse<String> response = httpClient.send(
                    request(method, path, params),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );
            ensureSuccess(response);
            return objectMapper.readValue(response.body(), type);
        } catch (Exception exception) {
            throw new IllegalStateException("No he podido consultar Trello: " + exception.getMessage(), exception);
        }
    }

    private void sendWithoutBody(String method, String path, Map<String, String> params) {
        try {
            HttpResponse<String> response = httpClient.send(
                    request(method, path, params),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );
            ensureSuccess(response);
        } catch (Exception exception) {
            throw new IllegalStateException("No he podido consultar Trello: " + exception.getMessage(), exception);
        }
    }

    private HttpRequest request(String method, String path, Map<String, String> params) {
        credentials.assertConfigured();
        URI uri = URI.create(credentials.apiBaseUrl() + path + "?" + buildQuery(params));
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/json");
        if ("POST".equalsIgnoreCase(method)) {
            builder.POST(HttpRequest.BodyPublishers.noBody());
        } else if ("PUT".equalsIgnoreCase(method)) {
            builder.PUT(HttpRequest.BodyPublishers.noBody());
        } else if ("DELETE".equalsIgnoreCase(method)) {
            builder.DELETE();
        } else {
            builder.GET();
        }
        return builder.build();
    }

    private String buildQuery(Map<String, String> params) {
        Map<String, String> effective = new LinkedHashMap<>();
        effective.put("key", credentials.apiKey());
        effective.put("token", credentials.accessToken());
        if (params != null) {
            effective.putAll(params);
        }
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : effective.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append('&');
            }
            builder.append(encode(entry.getKey())).append('=').append(encode(entry.getValue()));
        }
        return builder.toString();
    }

    private void ensureSuccess(HttpResponse<String> response) throws IOException {
        if (response.statusCode() >= 400) {
            throw new IOException("HTTP " + response.statusCode() + " - " + response.body());
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String encodePath(String value) {
        return value.replace("/", "%2F");
    }

    private void putIfNonBlank(Map<String, String> params, String key, String value) {
        if (value != null && !value.isBlank()) {
            params.put(key, value);
        }
    }

    private void putIfDefined(Map<String, String> params, String key, String value) {
        if (value != null) {
            params.put(key, value);
        }
    }
}
