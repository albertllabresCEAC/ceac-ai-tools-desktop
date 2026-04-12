package tools.ceac.ai.desktop.launcher;

import java.io.IOException;
import java.util.Map;
import org.springframework.context.ConfigurableApplicationContext;
import tools.ceac.ai.modules.trello.CeacTrelloRuntimeApplication;

/**
 * Runs the local Trello runtime as an embedded Spring context.
 *
 * <p>The Trello user token stays local to the desktop shell. The control plane only provisions the
 * public MCP surface and never sees the Trello account token captured from the browser.
 */
public class TrelloRuntimeService extends AbstractManagedSpringRuntimeService {

    private volatile ConfigurableApplicationContext managedContext;
    private volatile String swaggerUrl;

    public void start(BootstrapResponse bootstrap, ControlPlaneSession session, String apiKey, TrelloConnection connection)
            throws Exception {
        if (managedContext != null && managedContext.isActive()) {
            log("trello", "Reutilizando runtime Trello ya activo.");
            return;
        }
        if (connection == null || connection.accessToken() == null || connection.accessToken().isBlank()) {
            throw new IllegalStateException("Debes conectar Trello antes de arrancar el modulo.");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("No hay API key de Trello configurada.");
        }

        stopLegacyProcesses(
                bootstrap.localPort(),
                "CeacTrelloRuntimeApplication"
        );
        if (isLocalPortBusy(bootstrap.localPort())) {
            throw new IOException("El puerto " + bootstrap.localPort()
                    + " ya esta en uso. Cierra el proceso previo antes de arrancar Trello MCP.");
        }

        Map<String, Object> properties = standardRuntimeProperties(
                bootstrap,
                session,
                "ceac-ai-tools-trello-mcp",
                true,
                "ceac-trello-mcp",
                "Runtime MCP de CEAC AI Tools para Trello con autorizacion del operador via navegador."
        );
        properties.put("trello.api-base-url", "https://api.trello.com/1");
        properties.put("trello.api-key", apiKey);
        properties.put("trello.access-token", connection.accessToken());
        properties.put("trello.connected-member-id", connection.memberId());
        properties.put("trello.connected-username", connection.username());
        properties.put("trello.connected-full-name", connection.fullName());
        properties.put("trello.connected-profile-url", connection.profileUrl());

        log("trello", "Arrancando runtime Trello en puerto local " + bootstrap.localPort() + ".");
        managedContext = startContext(CeacTrelloRuntimeApplication.class, true, properties);

        try {
            waitForUrl(
                    localOAuthMetadataUrl(bootstrap),
                    90,
                    "No he podido arrancar Trello MCP en el tiempo esperado."
            );
            swaggerUrl = localSwaggerUrl(bootstrap);
            log("trello", "Trello MCP activo. Swagger: " + swaggerUrl);
        } catch (Exception exception) {
            stop();
            throw exception;
        }
    }

    public void stop() {
        ConfigurableApplicationContext context = managedContext;
        managedContext = null;
        swaggerUrl = null;
        if (context != null) {
            log("trello", "Parando runtime Trello.");
            context.close();
        }
    }

    public boolean isRunning() {
        return managedContext != null && managedContext.isActive();
    }

    public String getSwaggerUrl() {
        return swaggerUrl;
    }
}
