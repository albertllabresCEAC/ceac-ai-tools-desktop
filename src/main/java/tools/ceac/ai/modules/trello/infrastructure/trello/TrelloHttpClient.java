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
import tools.ceac.ai.modules.trello.application.auth.TrelloRuntimeCredentials;
import tools.ceac.ai.modules.trello.domain.model.CreateTrelloCardRequest;
import tools.ceac.ai.modules.trello.domain.model.TrelloBoardSummary;
import tools.ceac.ai.modules.trello.domain.model.TrelloCardSummary;
import tools.ceac.ai.modules.trello.domain.model.TrelloListSummary;
import tools.ceac.ai.modules.trello.domain.model.TrelloMemberProfile;

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

    public List<TrelloListSummary> listLists(String boardId) {
        return readList(
                "/boards/" + encodePath(boardId) + "/lists",
                Map.of("fields", "id,idBoard,name,closed,pos"),
                new TypeReference<>() {
                }
        );
    }

    public List<TrelloCardSummary> listCards(String listId) {
        return readList(
                "/lists/" + encodePath(listId) + "/cards",
                Map.of("fields", "id,idBoard,idList,name,desc,url,closed,due,idMembers"),
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

    private <T> T readObject(String path, Map<String, String> params, Class<T> type) {
        return sendObject("GET", path, params, type);
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

    private HttpRequest request(String method, String path, Map<String, String> params) {
        credentials.assertConfigured();
        URI uri = URI.create(credentials.apiBaseUrl() + path + "?" + buildQuery(params));
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/json");
        if ("POST".equalsIgnoreCase(method)) {
            builder.POST(HttpRequest.BodyPublishers.noBody());
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
            if (entry.getValue() == null || entry.getValue().isBlank()) {
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
}
