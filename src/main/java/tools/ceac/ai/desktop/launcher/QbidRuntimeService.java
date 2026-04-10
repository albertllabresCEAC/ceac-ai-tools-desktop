package tools.ceac.ai.desktop.launcher;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import tools.ceac.ai.desktop.ui.GuiLogPublisher;
import tools.ceac.ai.mcp.qbid.CeacQbidRuntimeApplication;
import tools.ceac.ai.mcp.qbid.mcp.McpAuthHelper;

/**
 * Runs the local qBid runtime as an embedded Spring context.
 *
 * <p>The old approach launched a second Maven project. That dependency is gone: qBid now lives
 * inside the desktop project and starts in an isolated Spring context on its own port.
 *
 * <p>The credentials handled here stay local to the machine. They are never forwarded to the
 * control plane.
 */
public class QbidRuntimeService {

    private static final String QBID_LOGIN_BASE_URL = "https://www.empresaiformacio.org/sBid";
    private static final String QBID_USER_AGENT = "Mozilla/5.0";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private volatile ConfigurableApplicationContext managedContext;
    private volatile String swaggerUrl;

    /**
     * Performs a real qBid login roundtrip to validate operator credentials before startup.
     */
    public void validateCredentials(String username, String password) throws Exception {
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        HttpClient client = HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(QBID_LOGIN_BASE_URL + "/modules/Login?initial=yes"))
                        .GET()
                        .header("User-Agent", QBID_USER_AGENT)
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        String body = Map.of(
                "idTask", "",
                "hashTask", "",
                "moduleaction", "doLogin",
                "fwBrowser", "Chrome",
                "username", username,
                "password", password
        ).entrySet().stream()
                .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8)
                        + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        HttpResponse<String> response = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(QBID_LOGIN_BASE_URL + "/modules/Login"))
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .header("User-Agent", QBID_USER_AGENT)
                        .header("Referer", QBID_LOGIN_BASE_URL + "/modules/Login?initial=yes")
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.body().contains("doLogin")) {
            throw new IOException("Usuario o contrasena qBid incorrectos.");
        }
    }

    /**
     * Starts qBid on the local port declared by bootstrap and waits until OAuth metadata is
     * reachable.
     */
    public void start(BootstrapResponse bootstrap, String username, String password) throws Exception {
        if (managedContext != null && managedContext.isActive()) {
            log("Reutilizando runtime qBid ya activo.");
            return;
        }

        stopLegacyExternalQbidProcesses(bootstrap.localPort());
        if (isLocalPortBusy(bootstrap.localPort())) {
            throw new IOException("El puerto " + bootstrap.localPort()
                    + " ya esta en uso. Cierra el proceso previo antes de arrancar qBid MCP.");
        }

        McpAuthHelper.setCredentials(username, password);
        Map<String, Object> properties = buildRuntimeProperties(bootstrap);
        log("Arrancando runtime qBid en puerto local " + bootstrap.localPort() + ".");

        managedContext = new SpringApplicationBuilder(CeacQbidRuntimeApplication.class)
                .headless(true)
                .run(toCommandLineArgs(properties));

        try {
            waitForUrl("http://localhost:" + bootstrap.localPort() + "/.well-known/oauth-protected-resource", 90);
            swaggerUrl = "http://localhost:" + bootstrap.localPort() + "/swagger-ui/index.html";
            log("QBid MCP activo. Swagger: " + swaggerUrl);
        } catch (Exception exception) {
            stop();
            throw exception;
        }
    }

    /**
     * Stops the managed qBid runtime if it is active.
     */
    public void stop() {
        ConfigurableApplicationContext context = managedContext;
        managedContext = null;
        swaggerUrl = null;
        if (context != null) {
            log("Parando runtime qBid.");
            context.close();
        }
    }

    /**
     * Indicates whether the embedded qBid Spring context is alive.
     */
    public boolean isRunning() {
        return managedContext != null && managedContext.isActive();
    }

    /**
     * Returns the local Swagger URL when the runtime is active.
     */
    public String getSwaggerUrl() {
        return swaggerUrl;
    }

    private Map<String, Object> buildRuntimeProperties(BootstrapResponse bootstrap) {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("server.port", bootstrap.localPort());
        properties.put("server.forward-headers-strategy", "framework");
        properties.put("spring.application.name", "ceac-ai-tools-qbid-mcp");
        properties.put("spring.main.headless", "true");
        properties.put("qbid.base-url", QBID_LOGIN_BASE_URL);
        properties.put("qbid.http.connect-timeout", "10000");
        properties.put("qbid.http.read-timeout", "30000");
        properties.put("spring.ai.mcp.server.name", "ceac-qbid-mcp");
        properties.put("spring.ai.mcp.server.version", "1.0.0");
        properties.put("spring.ai.mcp.server.type", "SYNC");
        properties.put("spring.ai.mcp.server.protocol", "STREAMABLE");
        properties.put("spring.ai.mcp.server.instructions", "Runtime MCP de CEAC AI Tools para qBID / sBID.");
        properties.put("spring.ai.mcp.server.streamable-http.mcp-endpoint", "/mcp");
        properties.put("mcp.remote.public-base-url", bootstrap.mcpPublicBaseUrl());
        properties.put("mcp.remote.mcp-endpoint", "/mcp");
        properties.put("mcp.remote.auth.enabled", "true");
        properties.put("mcp.remote.auth.issuer-uri", bootstrap.issuerUri());
        properties.put("mcp.remote.auth.jwk-set-uri", bootstrap.jwkSetUri());
        properties.put("mcp.remote.auth.required-audience", bootstrap.requiredAudience());
        properties.put("mcp.remote.auth.required-scope", bootstrap.requiredScope());
        properties.put("mcp.remote.auth.resource-name", bootstrap.resourceName());
        return properties;
    }

    String[] toCommandLineArgs(Map<String, Object> properties) {
        return properties.entrySet().stream()
                .map(entry -> "--" + entry.getKey() + "=" + entry.getValue())
                .toArray(String[]::new);
    }

    private void waitForUrl(String url, int timeoutSeconds) throws Exception {
        Instant deadline = Instant.now().plusSeconds(timeoutSeconds);
        while (Instant.now().isBefore(deadline)) {
            if (isUrlReady(url)) {
                return;
            }
            Thread.sleep(2000);
        }
        throw new IOException("No he podido arrancar qBid MCP en el tiempo esperado.");
    }

    private boolean isUrlReady(String url) {
        try {
            HttpResponse<Void> response = httpClient.send(
                    HttpRequest.newBuilder(URI.create(url))
                            .timeout(Duration.ofSeconds(5))
                            .GET()
                            .build(),
                    HttpResponse.BodyHandlers.discarding()
            );
            return response.statusCode() >= 200 && response.statusCode() < 500;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean isLocalPortBusy(int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", port), 500);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    private void stopLegacyExternalQbidProcesses(int port) {
        ProcessHandle.allProcesses()
                .filter(ProcessHandle::isAlive)
                .filter(handle -> {
                    String commandLine = handle.info().commandLine().orElse("");
                    return commandLine.contains("qBidScrAPI")
                            || commandLine.contains("com.tuapp.qbid.QbidApiApplication")
                            || commandLine.contains("--server.port=" + port);
                })
                .forEach(this::destroyProcessTree);
    }

    private void destroyProcessTree(ProcessHandle processHandle) {
        processHandle.descendants().forEach(descendant -> {
            try {
                descendant.destroyForcibly();
            } catch (Exception ignored) {
            }
        });
        try {
            processHandle.destroyForcibly();
        } catch (Exception ignored) {
        }
    }

    private void log(String message) {
        GuiLogPublisher.publish("[qbid] " + message + System.lineSeparator());
    }
}
