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

/**
 * Shared infrastructure for Spring-based MCP runtimes embedded by the desktop shell.
 *
 * <p>The concrete runtime services still decide how they validate prerequisites and whether they
 * expose additional UI components, but they all share the same mechanics:
 *
 * <ul>
 *   <li>clean stale processes from previous launches</li>
 *   <li>reserve a local port before startup</li>
 *   <li>launch a dedicated Spring context on that port</li>
 *   <li>wait for OAuth metadata to become reachable</li>
 *   <li>bind the REST API to {@code 127.0.0.1} while keeping the public MCP contract intact</li>
 *   <li>optionally trust launcher-issued local API tokens for Swagger and local REST checks</li>
 *   <li>publish launcher logs with a resource-specific prefix</li>
 * </ul>
 */
abstract class AbstractManagedSpringRuntimeService {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private final Path projectRoot = Paths.get("").toAbsolutePath().normalize();

    protected final Path projectRoot() {
        return projectRoot;
    }

    protected final ConfigurableApplicationContext startContext(Class<?> applicationClass,
                                                                boolean headless,
                                                                Map<String, Object> properties) {
        return new SpringApplicationBuilder(applicationClass)
                .headless(headless)
                .run(toCommandLineArgs(properties));
    }

    protected final Map<String, Object> standardRuntimeProperties(BootstrapResponse bootstrap,
                                                                  ControlPlaneSession session,
                                                                  String applicationName,
                                                                  boolean headless,
                                                                  String serverName,
                                                                  String instructions) {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("server.port", bootstrap.localPort());
        properties.put("server.address", "127.0.0.1");
        properties.put("server.forward-headers-strategy", "framework");
        properties.put("spring.application.name", applicationName);
        properties.put("spring.main.headless", Boolean.toString(headless));
        properties.put("spring.ai.mcp.server.name", serverName);
        properties.put("spring.ai.mcp.server.version", "1.0.0");
        properties.put("spring.ai.mcp.server.type", "SYNC");
        properties.put("spring.ai.mcp.server.protocol", "STATELESS");
        properties.put("spring.ai.mcp.server.instructions", instructions);
        properties.put("spring.ai.mcp.server.streamable-http.mcp-endpoint", "/mcp");
        properties.put("mcp.remote.public-base-url", bootstrap.mcpPublicBaseUrl());
        properties.put("mcp.remote.mcp-endpoint", "/mcp");
        properties.put("mcp.remote.auth.enabled", "true");
        properties.put("mcp.remote.auth.issuer-uri", bootstrap.issuerUri());
        properties.put("mcp.remote.auth.jwk-set-uri", bootstrap.jwkSetUri());
        properties.put("mcp.remote.auth.required-audience", bootstrap.requiredAudience());
        properties.put("mcp.remote.auth.required-scope", bootstrap.requiredScope());
        properties.put("mcp.remote.auth.resource-name", bootstrap.resourceName());
        if (session != null && session.accessLevel() != null) {
            properties.put("ceac.security.access-level", session.accessLevel().name());
        }
        if (session != null && session.launcherTokenIssuer() != null && !session.launcherTokenIssuer().isBlank()
                && session.launcherTokenSecret() != null && !session.launcherTokenSecret().isBlank()) {
            properties.put("mcp.remote.launcher.enabled", "true");
            properties.put("mcp.remote.launcher.issuer-uri", session.launcherTokenIssuer());
            properties.put("mcp.remote.launcher.shared-secret", session.launcherTokenSecret());
        }
        return properties;
    }

    protected final String localOAuthMetadataUrl(BootstrapResponse bootstrap) {
        return "http://localhost:" + bootstrap.localPort() + "/.well-known/oauth-protected-resource";
    }

    protected final String localSwaggerUrl(BootstrapResponse bootstrap) {
        return "http://localhost:" + bootstrap.localPort() + "/swagger-ui/index.html";
    }

    protected final void waitForUrl(String url, int timeoutSeconds, String timeoutMessage) throws Exception {
        Instant deadline = Instant.now().plusSeconds(timeoutSeconds);
        while (Instant.now().isBefore(deadline)) {
            if (isUrlReady(url)) {
                return;
            }
            Thread.sleep(2000);
        }
        throw new IOException(timeoutMessage);
    }

    protected final boolean isLocalPortBusy(int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", port), 500);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    protected final void stopLegacyProcesses(int port, String... markers) {
        ProcessHandle.allProcesses()
                .filter(ProcessHandle::isAlive)
                .filter(handle -> matchesProcess(handle, port, markers))
                .forEach(this::destroyProcessTree);
    }

    protected final void destroyProcessTree(ProcessHandle processHandle) {
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

    protected final void log(String channel, String message) {
        GuiLogPublisher.publish("[" + channel + "] " + message + System.lineSeparator());
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

    private boolean matchesProcess(ProcessHandle handle, int port, String[] markers) {
        String commandLine = handle.info().commandLine().orElse("");
        if (commandLine.contains("--server.port=" + port)) {
            return true;
        }
        for (String marker : markers) {
            if (commandLine.contains(marker)) {
                return true;
            }
        }
        return false;
    }

    protected final String[] toCommandLineArgs(Map<String, Object> properties) {
        return properties.entrySet().stream()
                .map(entry -> "--" + entry.getKey() + "=" + entry.getValue())
                .toArray(String[]::new);
    }
}

