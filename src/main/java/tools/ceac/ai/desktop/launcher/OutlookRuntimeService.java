package tools.ceac.ai.desktop.launcher;

import java.io.IOException;
import java.util.Map;
import org.springframework.context.ConfigurableApplicationContext;
import tools.ceac.ai.modules.outlook.OutlookMcpRuntimeApplication;

/**
 * Runs the local Outlook runtime as an embedded Spring context.
 *
 * <p>Outlook uses the same lifecycle model as the other CEAC AI Tools resources:
 *
 * <ul>
 *   <li>bootstrap comes from the control plane</li>
 *   <li>the launcher starts one local Spring context on the declared port</li>
 *   <li>the runtime becomes ready once OAuth metadata is reachable locally</li>
 * </ul>
 */
public class OutlookRuntimeService extends AbstractManagedSpringRuntimeService {

    private volatile ConfigurableApplicationContext managedContext;
    private volatile String swaggerUrl;

    /**
     * Starts the Outlook runtime on the local port declared by bootstrap.
     */
    public void start(BootstrapResponse bootstrap) throws Exception {
        if (managedContext != null && managedContext.isActive()) {
            log("outlook", "Reutilizando runtime Outlook ya activo.");
            return;
        }

        stopLegacyProcesses(
                bootstrap.localPort(),
                "OutlookMcpRuntimeApplication",
                "OutlookDesktopComMcpApplication"
        );
        if (isLocalPortBusy(bootstrap.localPort())) {
            throw new IOException("El puerto " + bootstrap.localPort()
                    + " ya esta en uso. Cierra el proceso previo antes de arrancar Outlook MCP.");
        }

        Map<String, Object> properties = standardRuntimeProperties(
                bootstrap,
                "ceac-ai-tools-outlook-mcp",
                false,
                "ceac-outlook-mcp",
                "Runtime MCP de CEAC AI Tools para Outlook Desktop en Windows mediante COM."
        );
        log("outlook", "Arrancando runtime Outlook en puerto local " + bootstrap.localPort() + ".");

        managedContext = startContext(OutlookMcpRuntimeApplication.class, false, properties);

        try {
            waitForUrl(
                    localOAuthMetadataUrl(bootstrap),
                    90,
                    "No he podido arrancar Outlook MCP en el tiempo esperado."
            );
            swaggerUrl = localSwaggerUrl(bootstrap);
            log("outlook", "Outlook MCP activo. Swagger: " + swaggerUrl);
        } catch (Exception exception) {
            stop();
            throw exception;
        }
    }

    /**
     * Stops the managed Outlook runtime if it is active.
     */
    public void stop() {
        ConfigurableApplicationContext context = managedContext;
        managedContext = null;
        swaggerUrl = null;
        if (context != null) {
            log("outlook", "Parando runtime Outlook.");
            context.close();
        }
    }

    /**
     * Indicates whether the embedded Outlook Spring context is alive.
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


