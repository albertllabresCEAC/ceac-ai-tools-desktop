package com.alber.outlookdesktop.launcher;

import com.alber.outlookdesktop.ui.GuiLogPublisher;
import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.util.StringUtils;

/**
 * Gestiona el runtime local de qBid como proceso independiente.
 */
public class QbidRuntimeService {

    private static final String QBID_LOGIN_BASE_URL = "https://www.empresaiformacio.org/sBid";
    private static final String QBID_USER_AGENT = "Mozilla/5.0";
    private static final DateTimeFormatter LOG_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private final Path launcherProjectRoot = Paths.get("").toAbsolutePath().normalize();
    private final Path logsDir = launcherProjectRoot.resolve("logs");
    private volatile Process managedProcess;
    private volatile String swaggerUrl;

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

    public void start(BootstrapResponse bootstrap, String username, String password) throws Exception {
        if (managedProcess != null && managedProcess.isAlive()) {
            log("Reutilizando runtime qBid ya activo.");
            return;
        }

        stopStaleProcessIfPortIsBusy(bootstrap.localPort());

        Path qbidRoot = resolveQbidProjectRoot();
        Path mvnw = qbidRoot.resolve("mvnw.cmd");
        if (!Files.exists(mvnw)) {
            throw new IOException("No encuentro mvnw.cmd en " + qbidRoot);
        }

        Files.createDirectories(logsDir);
        String timestamp = LOG_TIMESTAMP_FORMATTER.format(LocalDateTime.now());
        Path stdout = logsDir.resolve("qbid-runtime-" + timestamp + ".stdout.log");
        Path stderr = logsDir.resolve("qbid-runtime-" + timestamp + ".stderr.log");

        ProcessBuilder builder = new ProcessBuilder(
                "cmd.exe",
                "/c",
                mvnw.toString(),
                "-q",
                "spring-boot:run",
                "-Dspring-boot.run.arguments=--server.port=" + bootstrap.localPort()
        );
        builder.directory(qbidRoot.toFile());
        Map<String, String> environment = builder.environment();
        environment.put("SPRING_MAIN_HEADLESS", "true");
        environment.put("QBID_RUNTIME_USERNAME", username);
        environment.put("QBID_RUNTIME_PASSWORD", password);
        environment.put("MCP_PUBLIC_BASE_URL", bootstrap.mcpPublicBaseUrl());
        environment.put("MCP_AUTH_ENABLED", "true");
        environment.put("MCP_OAUTH_ISSUER_URI", bootstrap.issuerUri());
        environment.put("MCP_OAUTH_JWK_SET_URI", bootstrap.jwkSetUri());
        environment.put("MCP_OAUTH_REQUIRED_AUDIENCE", bootstrap.requiredAudience());
        environment.put("MCP_OAUTH_REQUIRED_SCOPE", bootstrap.requiredScope());
        environment.put("MCP_RESOURCE_NAME", bootstrap.resourceName());
        environment.put("MCP_ENDPOINT", "/mcp");
        builder.redirectOutput(stdout.toFile());
        builder.redirectError(stderr.toFile());

        managedProcess = builder.start();
        swaggerUrl = "http://localhost:" + bootstrap.localPort() + "/swagger-ui/index.html";
        waitForUrl("http://localhost:" + bootstrap.localPort() + "/.well-known/oauth-protected-resource", 90);
        log("QBid MCP activo. Swagger: " + swaggerUrl);
    }

    public void stop() {
        Process process = managedProcess;
        managedProcess = null;
        swaggerUrl = null;
        if (process != null && process.isAlive()) {
            log("Parando runtime qBid.");
            process.destroy();
            try {
                if (!process.waitFor(10, java.util.concurrent.TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                process.destroyForcibly();
            }
        }
    }

    public boolean isRunning() {
        return managedProcess != null && managedProcess.isAlive();
    }

    public String getSwaggerUrl() {
        return swaggerUrl;
    }

    private Path resolveQbidProjectRoot() throws IOException {
        String configured = System.getenv("CEAC_QBID_PROJECT_ROOT");
        Path candidate = StringUtils.hasText(configured)
                ? Paths.get(configured).toAbsolutePath().normalize()
                : launcherProjectRoot.getParent().resolve("qBidScrAPI").normalize();
        if (!Files.exists(candidate)) {
            throw new IOException("No encuentro el proyecto qBidScrAPI en " + candidate
                    + ". Define CEAC_QBID_PROJECT_ROOT para indicar su ruta.");
        }
        return candidate;
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

    private void log(String message) {
        GuiLogPublisher.publish("[qbid] " + message + System.lineSeparator());
    }

    private void stopStaleProcessIfPortIsBusy(int port) {
        if (!isLocalPortBusy(port)) {
            return;
        }
        log("Detectado un proceso previo ocupando el puerto " + port + ". Intentando liberarlo antes de arrancar qBid MCP.");
        ProcessHandle.allProcesses()
                .filter(ProcessHandle::isAlive)
                .filter(handle -> handle.info().commandLine().orElse("").contains("--server.port=" + port))
                .forEach(this::destroyProcessTree);
    }

    private boolean isLocalPortBusy(int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", port), 500);
            return true;
        } catch (IOException ex) {
            return false;
        }
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
}
