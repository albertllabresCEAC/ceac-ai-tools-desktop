package tools.ceac.ai.desktop.launcher;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.context.ConfigurableApplicationContext;
import tools.ceac.ai.modules.qbid.CeacQbidRuntimeApplication;

/**
 * Runs the local qBid runtime as an embedded Spring context.
 *
 * <p>qBid credentials stay local to the machine. The desktop launcher validates them before
 * startup, stores them only in memory and never forwards them to the control plane. The local API
 * remains bound to {@code localhost} and uses launcher-issued local tokens for Swagger and manual
 * REST checks.
 */
public class QbidRuntimeService extends AbstractManagedSpringRuntimeService {

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
    public void start(BootstrapResponse bootstrap, ControlPlaneSession session, String username, String password) throws Exception {
        if (managedContext != null && managedContext.isActive()) {
            log("qbid", "Reutilizando runtime qBid ya activo.");
            return;
        }

        stopLegacyProcesses(
                bootstrap.localPort(),
                "qBidScrAPI",
                "CeacQbidRuntimeApplication"
        );
        if (isLocalPortBusy(bootstrap.localPort())) {
            throw new IOException("El puerto " + bootstrap.localPort()
                    + " ya esta en uso. Cierra el proceso previo antes de arrancar qBid MCP.");
        }

        Map<String, Object> properties = standardRuntimeProperties(
                bootstrap,
                session,
                "ceac-ai-tools-qbid-mcp",
                true,
                "ceac-qbid-mcp",
                "Runtime MCP de CEAC AI Tools para qBID / sBID."
        );
        properties.put("qbid.base-url", QBID_LOGIN_BASE_URL);
        properties.put("qbid.http.connect-timeout", "10000");
        properties.put("qbid.http.read-timeout", "30000");
        properties.put("qbid.runtime.username", username);
        properties.put("qbid.runtime.password", password);

        log("qbid", "Arrancando runtime qBid en puerto local " + bootstrap.localPort() + ".");
        managedContext = startContext(CeacQbidRuntimeApplication.class, true, properties);

        try {
            waitForUrl(
                    localOAuthMetadataUrl(bootstrap),
                    90,
                    "No he podido arrancar qBid MCP en el tiempo esperado."
            );
            swaggerUrl = localSwaggerUrl(bootstrap);
            log("qbid", "QBid MCP activo. Swagger: " + swaggerUrl);
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
            log("qbid", "Parando runtime qBid.");
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
}


