package tools.ceac.ai.desktop.launcher;

import tools.ceac.ai.desktop.ui.GuiLogPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Servicio de infraestructura del launcher desktop.
 *
 * <p>Encapsula toda la logica no visual asociada al control plane:
 *
 * <ul>
 *   <li>login del desktop contra el backend</li>
 *   <li>peticion de bootstrap</li>
 *   <li>validacion minima del bootstrap recibido</li>
 *   <li>arranque y parada de {@code cloudflared}</li>
 *   <li>generacion de `.env.generated` para el runtime local</li>
 *   <li>verificacion de prerequisitos antes de arrancar el MCP</li>
 * </ul>
 *
 * <p>Esta clase asume explicitamente el modelo actual del sistema: el desktop consume un control
 * plane central y solo soporta {@code CENTRAL_AUTH}.
 */
public class RemoteLauncherService {

    private static final DateTimeFormatter LOG_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private final Path projectRoot = Paths.get("").toAbsolutePath().normalize();
    private final Path logsDir = projectRoot.resolve("logs");
    private final Path generatedEnvPath = projectRoot.resolve(".env.generated");

    private final Map<ManagedMcpKind, ManagedTunnelProcess> managedTunnelProcesses = new EnumMap<>(ManagedMcpKind.class);

    /**
     * Autentica al usuario desktop contra el control plane y devuelve una sesion lista para usar.
     */
    public ClientLoginResponse login(String controlPlaneBaseUrl, ClientLoginRequest request)
            throws IOException, InterruptedException {
        String baseUrl = normalizeBaseUrl(controlPlaneBaseUrl);
        String username = requireText(request.username(), "Debes indicar tu usuario o email.");
        String password = requireText(request.password(), "Debes indicar tu password.");

        String body = objectMapper.writeValueAsString(new ClientLoginRequest(
                username,
                password,
                request.machineId(),
                request.clientVersion()
        ));
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(baseUrl + "/api/client/login"))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() >= 400) {
            throw new IOException("El control plane devolvio HTTP " + response.statusCode() + ": " + response.body());
        }

        ClientLoginResponse loginResponse = objectMapper.readValue(response.body(), ClientLoginResponse.class);
        if (loginResponse.bootstrap() == null) {
            throw new IOException("El login no devolvio bootstrap.");
        }
        if (!StringUtils.hasText(loginResponse.accessToken())) {
            throw new IOException("El login no devolvio accessToken.");
        }
        validateBootstrap(loginResponse.bootstrap());
        return loginResponse;
    }

    /**
     * Pide bootstrap autenticado al control plane usando el token del desktop.
     */
    public BootstrapResponse fetchBootstrap(String controlPlaneBaseUrl, String bearerToken, BootstrapRequest request)
            throws IOException, InterruptedException {
        String baseUrl = normalizeBaseUrl(controlPlaneBaseUrl);
        String token = requireText(bearerToken, "Debes indicar el bearer token del control plane.");
        if (!StringUtils.hasText(request.externalUserId())) {
            throw new IOException("Debes indicar externalUserId para pedir bootstrap.");
        }

        String body = objectMapper.writeValueAsString(request);
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(baseUrl + "/api/client/bootstrap"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() >= 400) {
            throw new IOException("El control plane devolvio HTTP " + response.statusCode() + ": " + response.body());
        }

        BootstrapResponse bootstrap = objectMapper.readValue(response.body(), BootstrapResponse.class);
        validateBootstrap(bootstrap);
        return bootstrap;
    }

    /**
     * Traduce el bootstrap recibido a las properties Spring con las que se arranca la app local.
     */
    public RuntimeSettings runtimeSettingsFromBootstrap(BootstrapResponse bootstrap) {
        return new RuntimeSettings(
                bootstrap.mcpPublicBaseUrl(),
                bootstrap.authPublicBaseUrl(),
                bootstrap.issuerUri(),
                bootstrap.jwkSetUri(),
                bootstrap.requiredAudience(),
                bootstrap.requiredScope(),
                bootstrap.resourceName()
        );
    }

    /**
     * Arranca o reutiliza un proceso local de {@code cloudflared} usando el token remoto del bootstrap.
     */
    public void startManagedTunnel(BootstrapResponse bootstrap) throws IOException, InterruptedException {
        ManagedMcpKind kind = resolveKind(bootstrap.resourceKey());
        startManagedTunnel(kind, bootstrap);
    }

    public void startManagedTunnel(ManagedMcpKind kind, BootstrapResponse bootstrap) throws IOException, InterruptedException {
        if (!bootstrap.cloudflaredManagedRemotely()) {
            throw new IOException("El bootstrap actual no permite gestionar cloudflared automaticamente.");
        }

        String cloudflaredCommand = resolveCloudflaredCommand();
        ManagedTunnelProcess currentProcess = managedTunnelProcesses.get(kind);
        if (currentProcess != null && currentProcess.process().isAlive()) {
            log("Reutilizando cloudflared ya lanzado para " + kind.displayName() + ".");
            return;
        }

        String metricsUrl = metricsUrl(kind);
        if (isUrlReady(metricsUrl)) {
            log("Reutilizando cloudflared ya activo para " + kind.displayName() + ".");
            return;
        }

        CloudflaredLogFiles logFiles = createCloudflaredLogFiles(kind);

        log("Arrancando cloudflared para " + kind.displayName() + " con tunnel token remoto.");
        ProcessBuilder builder = new ProcessBuilder(
                cloudflaredCommand,
                "--metrics",
                "127.0.0.1:" + kind.metricsPort(),
                "tunnel",
                "run",
                "--token",
                bootstrap.tunnelToken()
        );
        builder.directory(projectRoot.toFile());
        builder.redirectOutput(logFiles.stdout().toFile());
        builder.redirectError(logFiles.stderr().toFile());
        Process process = builder.start();
        managedTunnelProcesses.put(kind, new ManagedTunnelProcess(process, logFiles.stdout(), logFiles.stderr()));

        Thread.sleep(3000);
        if (!process.isAlive()) {
            String error = readTail(logFiles.stdout()) + System.lineSeparator() + readTail(logFiles.stderr());
            throw new IOException("cloudflared ha terminado nada mas arrancar. " + error.trim());
        }

        waitForUrl(metricsUrl, 30);
        log("Tunel activo para " + kind.displayName() + ". Logs: "
                + projectRoot.relativize(logFiles.stdout()) + " y " + projectRoot.relativize(logFiles.stderr()));
    }

    /**
     * Para el proceso de {@code cloudflared} gestionado por esta instancia, si existe.
     */
    public void stopTunnel() {
        for (ManagedMcpKind kind : ManagedMcpKind.values()) {
            stopTunnel(kind);
        }
    }

    public void stopTunnel(ManagedMcpKind kind) {
        ManagedTunnelProcess managedTunnelProcess = managedTunnelProcesses.remove(kind);
        Process process = managedTunnelProcess == null ? null : managedTunnelProcess.process();
        if (process != null && process.isAlive()) {
            log("Parando cloudflared para " + kind.displayName() + ".");
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

    /**
     * Genera el archivo `.env.generated` que materializa en disco la configuracion del bootstrap.
     */
    public void writeGeneratedEnv(RuntimeSettings runtimeSettings) throws IOException {
        String content = String.join(System.lineSeparator(),
                "MCP_PUBLIC_BASE_URL=" + runtimeSettings.mcpPublicBaseUrl(),
                "MCP_AUTH_ENABLED=true",
                "MCP_OAUTH_ISSUER_URI=" + runtimeSettings.issuerUri(),
                "MCP_OAUTH_JWK_SET_URI=" + runtimeSettings.jwkSetUri(),
                "MCP_OAUTH_REQUIRED_AUDIENCE=" + runtimeSettings.requiredAudience(),
                "MCP_OAUTH_REQUIRED_SCOPE=" + runtimeSettings.requiredScope(),
                "MCP_RESOURCE_NAME=" + runtimeSettings.resourceName()
        );
        Files.writeString(projectRoot.resolve(".env.generated"), content, StandardCharsets.US_ASCII);
        log("Variables guardadas en " + generatedEnvPath);
    }

    /**
     * Devuelve la lista de bloqueos que impiden arrancar el MCP local.
     */
    public List<String> validatePrerequisites(ControlPlaneSession session) {
        return validatePrerequisites(session, null);
    }

    public List<String> validatePrerequisites(ControlPlaneSession session, ManagedMcpKind kind) {
        List<String> errors = new ArrayList<>();
        if (session == null) {
            errors.add("No hay una sesion activa cargada desde Login.");
            return errors;
        }

        if (!session.usesCentralAuth()) {
            errors.add("Este launcher solo soporta CENTRAL_AUTH. Ajusta el backend para no depender de Keycloak local.");
        }

        if (kind != null && session.bootstrapFor(kind.resourceKey()) == null) {
            errors.add("No hay bootstrap cargado para " + kind.displayName() + ".");
        }

        try {
            resolveCloudflaredCommand();
        } catch (IOException ex) {
            errors.add(ex.getMessage());
        }

        return errors;
    }

    /**
     * Comprueba si una URL responde de forma util para el launcher.
     */
    public boolean isUrlReady(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            return response.statusCode() >= 200 && response.statusCode() < 500;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Resuelve el ejecutable `cloudflared` desde variable de entorno, PATH o rutas habituales.
     */
    public String resolveCloudflaredCommand() throws IOException {
        String configured = System.getenv("CLOUDFLARED_CMD");
        if (StringUtils.hasText(configured) && Files.exists(Paths.get(configured))) {
            return configured;
        }

        Optional<String> fromPath = findExecutableInPath("cloudflared.exe").or(() -> findExecutableInPath("cloudflared"));
        if (fromPath.isPresent()) {
            return fromPath.get();
        }

        Path home = Paths.get(System.getProperty("user.home"));
        List<Path> candidates = List.of(
                Paths.get("C:\\Program Files\\cloudflared\\cloudflared.exe"),
                Paths.get("C:\\Program Files (x86)\\cloudflared\\cloudflared.exe"),
                home.resolve(".cloudflared").resolve("cloudflared.exe")
        );
        for (Path candidate : candidates) {
            if (Files.exists(candidate)) {
                return candidate.toString();
            }
        }
        throw new IOException("No encuentro 'cloudflared'. Añadelo al PATH o define CLOUDFLARED_CMD.");
    }

    /**
     * Libera el proceso gestionado cuando Spring destruye el bean o la app sale.
     */
    @PreDestroy
    public void shutdown() {
        stopTunnel();
    }

    public boolean isTunnelReady(ManagedMcpKind kind) {
        return isUrlReady(metricsUrl(kind));
    }

    public String metricsUrl(ManagedMcpKind kind) {
        return "http://127.0.0.1:" + kind.metricsPort() + "/metrics";
    }

    private void validateBootstrap(BootstrapResponse bootstrap) throws IOException {
        if (!StringUtils.hasText(bootstrap.tunnelToken())) {
            throw new IOException("El bootstrap no incluye tunnelToken.");
        }
        if (!StringUtils.hasText(bootstrap.mcpPublicBaseUrl())) {
            throw new IOException("El bootstrap no incluye mcpPublicBaseUrl.");
        }
        if (!StringUtils.hasText(bootstrap.issuerUri())) {
            throw new IOException("El bootstrap no incluye issuerUri.");
        }
        if (!StringUtils.hasText(bootstrap.jwkSetUri())) {
            throw new IOException("El bootstrap no incluye jwkSetUri.");
        }
    }

    private Optional<String> findExecutableInPath(String executable) {
        String path = System.getenv("PATH");
        if (!StringUtils.hasText(path)) {
            return Optional.empty();
        }
        for (String part : path.split(Pattern.quote(java.io.File.pathSeparator))) {
            if (!StringUtils.hasText(part)) {
                continue;
            }
            Path candidate = Paths.get(part).resolve(executable);
            if (Files.exists(candidate)) {
                return Optional.of(candidate.toString());
            }
        }
        return Optional.empty();
    }

    private void waitForUrl(String url, int timeoutSeconds) throws IOException, InterruptedException {
        Instant deadline = Instant.now().plusSeconds(timeoutSeconds);
        while (Instant.now().isBefore(deadline)) {
            if (isUrlReady(url)) {
                return;
            }
            Thread.sleep(2000);
        }
        throw new IOException("No he podido obtener respuesta de " + url + " en " + timeoutSeconds + " segundos.");
    }

    private String readStream(InputStream inputStream) throws IOException {
        try (InputStream in = inputStream; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            in.transferTo(out);
            return out.toString(StandardCharsets.UTF_8);
        }
    }

    private String requireText(String value, String message) throws IOException {
        if (!StringUtils.hasText(value)) {
            throw new IOException(message);
        }
        return value.trim();
    }

    private String normalizeBaseUrl(String value) throws IOException {
        String baseUrl = requireText(value, "Debes indicar la URL base del control plane.");
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    private String readTail(Path path) throws IOException {
        if (path == null || !Files.exists(path)) {
            return "";
        }
        List<String> lines = Files.readAllLines(path);
        int start = Math.max(0, lines.size() - 40);
        return String.join(System.lineSeparator(), lines.subList(start, lines.size()));
    }

    private CloudflaredLogFiles createCloudflaredLogFiles(ManagedMcpKind kind) throws IOException {
        Files.createDirectories(logsDir);
        String timestamp = LOG_TIMESTAMP_FORMATTER.format(LocalDateTime.now());
        return new CloudflaredLogFiles(
                logsDir.resolve("cloudflared-" + kind.resourceKey() + "-" + timestamp + ".stdout.log"),
                logsDir.resolve("cloudflared-" + kind.resourceKey() + "-" + timestamp + ".stderr.log")
        );
    }

    private void log(String message) {
        GuiLogPublisher.publish("[launcher] " + message + System.lineSeparator());
    }

    private record CloudflaredLogFiles(Path stdout, Path stderr) {
    }

    private ManagedMcpKind resolveKind(String resourceKey) throws IOException {
        for (ManagedMcpKind kind : ManagedMcpKind.values()) {
            if (kind.resourceKey().equalsIgnoreCase(resourceKey)) {
                return kind;
            }
        }
        throw new IOException("Recurso MCP no soportado por el launcher: " + resourceKey);
    }

    private record ManagedTunnelProcess(Process process, Path stdout, Path stderr) {
    }
}

