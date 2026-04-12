package tools.ceac.ai.modules.trello.application.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Local Trello credentials injected into the embedded runtime by the launcher.
 */
@Component
public class TrelloRuntimeCredentials {

    private final String apiBaseUrl;
    private final String apiKey;
    private final String accessToken;
    private final String connectedMemberId;
    private final String connectedUsername;
    private final String connectedFullName;
    private final String connectedProfileUrl;

    public TrelloRuntimeCredentials(
            @Value("${trello.api-base-url:https://api.trello.com/1}") String apiBaseUrl,
            @Value("${trello.api-key:}") String apiKey,
            @Value("${trello.access-token:}") String accessToken,
            @Value("${trello.connected-member-id:}") String connectedMemberId,
            @Value("${trello.connected-username:}") String connectedUsername,
            @Value("${trello.connected-full-name:}") String connectedFullName,
            @Value("${trello.connected-profile-url:}") String connectedProfileUrl
    ) {
        this.apiBaseUrl = apiBaseUrl;
        this.apiKey = apiKey;
        this.accessToken = accessToken;
        this.connectedMemberId = connectedMemberId;
        this.connectedUsername = connectedUsername;
        this.connectedFullName = connectedFullName;
        this.connectedProfileUrl = connectedProfileUrl;
    }

    public String apiBaseUrl() {
        return apiBaseUrl;
    }

    public String apiKey() {
        return apiKey;
    }

    public String accessToken() {
        return accessToken;
    }

    public String connectedMemberId() {
        return connectedMemberId;
    }

    public String connectedUsername() {
        return connectedUsername;
    }

    public String connectedFullName() {
        return connectedFullName;
    }

    public String connectedProfileUrl() {
        return connectedProfileUrl;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank() && accessToken != null && !accessToken.isBlank();
    }

    public void assertConfigured() {
        if (!isConfigured()) {
            throw new IllegalStateException("No hay conexion Trello cargada en el runtime local.");
        }
    }
}
