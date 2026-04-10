package tools.ceac.ai.desktop.launcher;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import tools.ceac.ai.desktop.ui.GuiLogPublisher;
import tools.ceac.ai.mcp.campus.CeacCampusRuntimeApplication;
import tools.ceac.ai.mcp.campus.interfaces.desktop.CampusEmbeddedPanel;

/**
 * Runs the local Campus runtime as an embedded Spring context and exposes the JCEF panel mounted
 * inside the {@code Campus MCP} tab.
 *
 * <p>Campus differs from qBid because it has an embedded-browser authentication flow. This service
 * therefore owns both runtime lifecycle and the bridge between the launcher tab and the Campus
 * JCEF panel.
 */
public class CampusRuntimeService {

    private static final String CAMPUS_BASE_URL = "https://campus.ceacfp.es";
    private static final String CAMPUS_LOGIN_URL = "https://campus.ceacfp.es/login/index.php";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private final Path projectRoot = Paths.get("").toAbsolutePath().normalize();

    private volatile ConfigurableApplicationContext managedContext;
    private volatile CampusEmbeddedPanel embeddedPanel;
    private volatile String swaggerUrl;

    /**
     * Starts the Campus runtime and returns the embeddable JCEF panel used by the launcher tab.
     */
    public CampusEmbeddedPanel start(BootstrapResponse bootstrap) throws Exception {
        if (managedContext != null && managedContext.isActive() && embeddedPanel != null) {
            log("Reutilizando runtime Campus ya activo.");
            return embeddedPanel;
        }

        stopLegacyExternalCampusProcesses(bootstrap.localPort());
        if (isLocalPortBusy(bootstrap.localPort())) {
            throw new IOException("El puerto " + bootstrap.localPort()
                    + " ya esta en uso. Cierra el proceso previo antes de arrancar Campus MCP.");
        }

        Map<String, Object> properties = buildRuntimeProperties(bootstrap);
        log("Arrancando runtime Campus en puerto local " + bootstrap.localPort() + ".");

        managedContext = new SpringApplicationBuilder(CeacCampusRuntimeApplication.class)
                .headless(false)
                .run(toCommandLineArgs(properties));

        try {
            embeddedPanel = managedContext.getBean(CampusEmbeddedPanel.class);
            waitForUrl("http://localhost:" + bootstrap.localPort() + "/.well-known/oauth-protected-resource", 90);
            swaggerUrl = "http://localhost:" + bootstrap.localPort() + "/swagger-ui/index.html";
            log("Campus MCP activo. Swagger: " + swaggerUrl);
            return embeddedPanel;
        } catch (Exception exception) {
            stop();
            throw exception;
        }
    }

    /**
     * Stops the managed Campus runtime and clears the current embedded browser panel.
     */
    public void stop() {
        ConfigurableApplicationContext context = managedContext;
        managedContext = null;
        embeddedPanel = null;
        swaggerUrl = null;
        if (context != null) {
            log("Parando runtime Campus.");
            context.close();
        }
    }

    /**
     * Indicates whether the embedded Campus Spring context is alive.
     */
    public boolean isRunning() {
        return managedContext != null && managedContext.isActive();
    }

    /**
     * Returns the active embedded panel, or {@code null} when Campus is not running.
     */
    public CampusEmbeddedPanel getEmbeddedPanel() {
        return embeddedPanel;
    }

    /**
     * Returns the local Swagger URL when the runtime is active.
     */
    public String getSwaggerUrl() {
        return swaggerUrl;
    }

    String[] toCommandLineArgs(Map<String, Object> properties) {
        return properties.entrySet().stream()
                .map(entry -> "--" + entry.getKey() + "=" + entry.getValue())
                .toArray(String[]::new);
    }

    private Map<String, Object> buildRuntimeProperties(BootstrapResponse bootstrap) {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("server.port", bootstrap.localPort());
        properties.put("server.forward-headers-strategy", "framework");
        properties.put("spring.application.name", "ceac-ai-tools-campus-mcp");
        properties.put("spring.main.headless", "false");
        properties.put("campus.base-url", CAMPUS_BASE_URL);
        properties.put("campus.login-url", CAMPUS_LOGIN_URL);
        properties.put("campus.dashboard-path", "/my/");
        properties.put("campus.http-timeout-seconds", "30");
        properties.put("campus.jcef-install-dir", projectRoot.resolve("jcef-bundle").resolve("campus"));
        properties.put("campus.jcef-cache-dir", projectRoot.resolve("jcef-cache").resolve("campus"));
        properties.put("campus.ui.enabled", "true");
        properties.put("campus.ui.show-logout", "true");
        properties.put("campus.ui.show-cookies", "true");
        properties.put("campus.ui.show-current-url", "true");
        properties.put("campus.ui.show-browser-view", "true");
        properties.put("spring.ai.mcp.server.name", "ceac-campus-mcp");
        properties.put("spring.ai.mcp.server.version", "1.0.0");
        properties.put("spring.ai.mcp.server.type", "SYNC");
        properties.put("spring.ai.mcp.server.protocol", "STREAMABLE");
        properties.put("spring.ai.mcp.server.instructions",
                "Runtime MCP de CEAC AI Tools para CEAC Campus con sesion Moodle compartida desde navegador embebido.");
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

    private void waitForUrl(String url, int timeoutSeconds) throws Exception {
        Instant deadline = Instant.now().plusSeconds(timeoutSeconds);
        while (Instant.now().isBefore(deadline)) {
            if (isUrlReady(url)) {
                return;
            }
            Thread.sleep(2000);
        }
        throw new IOException("No he podido arrancar Campus MCP en el tiempo esperado.");
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

    private void stopLegacyExternalCampusProcesses(int port) {
        ProcessHandle.allProcesses()
                .filter(ProcessHandle::isAlive)
                .filter(handle -> {
                    String commandLine = handle.info().commandLine().orElse("");
                    return commandLine.contains("campusScrAPI")
                            || commandLine.contains("CampusScrApiApplication")
                            || commandLine.contains("CeacCampusRuntimeApplication")
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
        GuiLogPublisher.publish("[campus] " + message + System.lineSeparator());
    }
}
