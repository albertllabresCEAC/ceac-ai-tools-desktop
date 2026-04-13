package tools.ceac.ai.desktop.launcher;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Handles the browser-based Trello authorization flow for the desktop shell.
 *
 * <p>Trello returns the token in the URL fragment, so the launcher exposes a small localhost page
 * that captures the fragment in JavaScript and posts it back to a local endpoint.
 *
 * <p>The Trello application configuration must allow the localhost callback base
 * {@code http://127.0.0.1:43127}. The captured token stays on the operator machine and never
 * travels to the control plane.
 */
public class TrelloAuthorizationService {

    static final int CALLBACK_PORT = 43127;
    static final String CALLBACK_BASE_URL = "http://127.0.0.1:" + CALLBACK_PORT;
    private static final String CALLBACK_PATH = "/trello/callback";
    private static final String CAPTURE_PATH = "/trello/token";
    private static final String AUTHORIZE_BASE_URL = "https://trello.com/1/authorize";
    private static final String API_BASE_URL = "https://api.trello.com/1";

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private final Object lock = new Object();

    private HttpServer callbackServer;
    private String pendingState;
    private CompletableFuture<CapturePayload> pendingCapture;

    /**
     * Starts one browser authorization round-trip and returns the validated Trello connection bound
     * to the current desktop session.
     */
    public TrelloConnection authorize(String apiKey) throws Exception {
        String resolvedApiKey = requireApiKey(apiKey);
        ensureServerStarted();

        String state;
        CompletableFuture<CapturePayload> future;
        synchronized (lock) {
            if (pendingCapture != null && !pendingCapture.isDone()) {
                throw new IllegalStateException("Ya hay una autorizacion de Trello en curso.");
            }
            state = UUID.randomUUID().toString();
            future = new CompletableFuture<>();
            pendingState = state;
            pendingCapture = future;
        }

        try {
            openBrowser(buildAuthorizeUrl(resolvedApiKey, state));
            CapturePayload payload = future.get(5, TimeUnit.MINUTES);
            if (payload.error() != null && !payload.error().isBlank()) {
                throw new IllegalStateException("Trello devolvio un error de autorizacion: " + payload.error());
            }
            if (payload.token() == null || payload.token().isBlank()) {
                throw new IllegalStateException("No he recibido un token de Trello en el callback local.");
            }
            return resolveConnection(resolvedApiKey, payload.token());
        } catch (ExecutionException exception) {
            Throwable cause = exception.getCause() == null ? exception : exception.getCause();
            if (cause instanceof Exception wrapped) {
                throw wrapped;
            }
            throw new IllegalStateException(cause.getMessage(), cause);
        } finally {
            synchronized (lock) {
                pendingState = null;
                pendingCapture = null;
            }
        }
    }

    /**
     * Validates an existing Trello token and rebuilds the current member connection metadata.
     */
    public TrelloConnection resolveConnection(String apiKey, String accessToken) throws IOException, InterruptedException {
        return fetchCurrentMember(requireApiKey(apiKey), requireAccessToken(accessToken));
    }

    /**
     * Revokes an existing Trello token. Tokens that are already expired or unknown are treated as
     * already disconnected.
     */
    public void revoke(String apiKey, String accessToken) throws IOException, InterruptedException {
        String resolvedApiKey = requireApiKey(apiKey);
        String resolvedAccessToken = requireAccessToken(accessToken);
        URI uri = URI.create(API_BASE_URL + "/tokens/"
                + encode(resolvedAccessToken)
                + "?key=" + encode(resolvedApiKey)
                + "&token=" + encode(resolvedAccessToken));
        HttpResponse<String> response = httpClient.send(
                HttpRequest.newBuilder(uri)
                        .timeout(Duration.ofSeconds(20))
                        .DELETE()
                        .build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
        );
        if (response.statusCode() == 401 || response.statusCode() == 404) {
            return;
        }
        if (response.statusCode() >= 400) {
            throw new IOException("Trello no ha podido revocar el token con HTTP " + response.statusCode() + ".");
        }
    }

    /**
     * Stops the localhost callback server and cancels any pending browser authorization.
     */
    public void shutdown() {
        synchronized (lock) {
            if (callbackServer != null) {
                callbackServer.stop(0);
                callbackServer = null;
            }
            if (pendingCapture != null && !pendingCapture.isDone()) {
                pendingCapture.completeExceptionally(new IllegalStateException("Autorizacion de Trello cancelada."));
            }
            pendingCapture = null;
            pendingState = null;
        }
    }

    private void ensureServerStarted() throws IOException {
        synchronized (lock) {
            if (callbackServer != null) {
                return;
            }
            HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", CALLBACK_PORT), 0);
            server.createContext(CALLBACK_PATH, this::handleCallbackPage);
            server.createContext(CAPTURE_PATH, this::handleCaptureToken);
            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
            callbackServer = server;
        }
    }

    private void handleCallbackPage(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendText(exchange, 405, "Method not allowed", "text/plain; charset=utf-8");
            return;
        }
        String html = """
                <!doctype html>
                <html lang="es">
                <head>
                  <meta charset="utf-8">
                  <title>CEAC AI Tools - Trello</title>
                  <style>
                    body { font-family: Segoe UI, Arial, sans-serif; background: #f2f4f8; color: #0f172a; margin: 0; padding: 32px; }
                    .card { max-width: 560px; margin: 48px auto; background: #ffffff; border: 1px solid #d7deea; border-radius: 16px; padding: 28px 32px; box-shadow: 0 16px 40px rgba(15,23,42,0.08); }
                    h1 { margin: 0 0 12px; font-size: 22px; }
                    p { margin: 8px 0; line-height: 1.5; }
                    .muted { color: #667085; }
                  </style>
                </head>
                <body>
                  <div class="card">
                    <h1>Conectando Trello</h1>
                    <p id="status">Procesando autorizacion...</p>
                    <p class="muted">Puedes cerrar esta ventana cuando aparezca el mensaje de confirmacion.</p>
                  </div>
                  <script>
                    const hash = new URLSearchParams(window.location.hash.startsWith('#') ? window.location.hash.substring(1) : '');
                    const query = new URLSearchParams(window.location.search);
                    const payload = {
                      state: query.get('state'),
                      token: hash.get('token'),
                      error: hash.get('error')
                    };
                    const status = document.getElementById('status');
                    fetch(window.location.origin + '/trello/token', {
                      method: 'POST',
                      headers: { 'Content-Type': 'application/json' },
                      body: JSON.stringify(payload)
                    }).then(response => {
                      if (!response.ok) {
                        throw new Error('No se ha podido confirmar la autorizacion en el launcher.');
                      }
                      status.textContent = payload.token
                        ? 'Conexion completada. Ya puedes volver a CEAC AI Tools.'
                        : 'No se ha recibido token de Trello.';
                    }).catch(error => {
                      status.textContent = error.message;
                    });
                  </script>
                </body>
                </html>
                """;
        sendText(exchange, 200, html, "text/html; charset=utf-8");
    }

    private void handleCaptureToken(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendText(exchange, 405, "Method not allowed", "text/plain; charset=utf-8");
            return;
        }
        CapturePayload payload = objectMapper.readValue(exchange.getRequestBody(), CapturePayload.class);
        CompletableFuture<CapturePayload> future;
        String expectedState;
        synchronized (lock) {
            future = pendingCapture;
            expectedState = pendingState;
        }
        if (future == null) {
            sendText(exchange, 409, "No pending authorization", "text/plain; charset=utf-8");
            return;
        }
        if (!Objects.equals(expectedState, payload.state())) {
            future.completeExceptionally(new IllegalStateException("El callback de Trello ha llegado con un state invalido."));
            sendText(exchange, 400, "Invalid state", "text/plain; charset=utf-8");
            return;
        }
        future.complete(payload);
        sendText(exchange, 200, "{\"status\":\"ok\"}", "application/json; charset=utf-8");
    }

    private TrelloConnection fetchCurrentMember(String apiKey, String token) throws IOException, InterruptedException {
        URI uri = URI.create(API_BASE_URL + "/members/me?key="
                + encode(apiKey)
                + "&token=" + encode(token)
                + "&fields=id,username,fullName,url");
        HttpResponse<String> response = httpClient.send(
                HttpRequest.newBuilder(uri)
                        .timeout(Duration.ofSeconds(20))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
        );
        if (response.statusCode() >= 400) {
            throw new IOException("Trello rechazo el token con HTTP " + response.statusCode() + ".");
        }
        TrelloMemberResponse member = objectMapper.readValue(response.body(), TrelloMemberResponse.class);
        return new TrelloConnection(token, member.id(), member.username(), member.fullName(), member.url());
    }

    private String buildAuthorizeUrl(String apiKey, String state) {
        String returnUrl = CALLBACK_BASE_URL + CALLBACK_PATH + "?state=" + encode(state);
        return AUTHORIZE_BASE_URL
                + "?key=" + encode(apiKey)
                + "&name=" + encode("CEAC AI Tools")
                + "&scope=" + encode("read,write")
                + "&expiration=" + encode("never")
                + "&response_type=token"
                + "&callback_method=fragment"
                + "&return_url=" + encode(returnUrl);
    }

    private void openBrowser(String url) throws Exception {
        if (!Desktop.isDesktopSupported()) {
            throw new IllegalStateException("No hay soporte Desktop para abrir el navegador.");
        }
        Desktop.getDesktop().browse(URI.create(url));
    }

    private String requireApiKey(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("No hay API key de Trello configurada.");
        }
        return apiKey.trim();
    }

    private String requireAccessToken(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalStateException("No hay token Trello disponible.");
        }
        return accessToken.trim();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private void sendText(HttpExchange exchange, int statusCode, String body, String contentType) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
        } finally {
            exchange.close();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record TrelloMemberResponse(String id, String username, String fullName, String url) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record CapturePayload(String state, String token, String error) {
    }
}
