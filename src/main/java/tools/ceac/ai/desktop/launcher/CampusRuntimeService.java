package tools.ceac.ai.desktop.launcher;

import java.io.IOException;
import java.util.Map;
import org.springframework.context.ConfigurableApplicationContext;
import tools.ceac.ai.modules.campus.CeacCampusRuntimeApplication;
import tools.ceac.ai.modules.campus.interfaces.desktop.CampusEmbeddedPanel;

/**
 * Runs the local Campus runtime as an embedded Spring context and exposes the JCEF panel mounted
 * inside the {@code Campus MCP} tab.
 *
 * <p>Campus differs from the other modules because it embeds a browser-based login flow. This
 * service therefore owns both runtime lifecycle and the bridge between the launcher tab and the
 * Campus panel.
 */
public class CampusRuntimeService extends AbstractManagedSpringRuntimeService {

    private static final String CAMPUS_BASE_URL = "https://campus.ceacfp.es";
    private static final String CAMPUS_LOGIN_URL = "https://campus.ceacfp.es/login/index.php";

    private volatile ConfigurableApplicationContext managedContext;
    private volatile CampusEmbeddedPanel embeddedPanel;
    private volatile String swaggerUrl;

    /**
     * Starts the Campus runtime and returns the embeddable JCEF panel used by the launcher tab.
     */
    public CampusEmbeddedPanel start(BootstrapResponse bootstrap) throws Exception {
        if (managedContext != null && managedContext.isActive() && embeddedPanel != null) {
            log("campus", "Reutilizando runtime Campus ya activo.");
            return embeddedPanel;
        }

        stopLegacyProcesses(
                bootstrap.localPort(),
                "campusScrAPI",
                "CampusScrApiApplication",
                "CeacCampusRuntimeApplication"
        );
        if (isLocalPortBusy(bootstrap.localPort())) {
            throw new IOException("El puerto " + bootstrap.localPort()
                    + " ya esta en uso. Cierra el proceso previo antes de arrancar Campus MCP.");
        }

        Map<String, Object> properties = standardRuntimeProperties(
                bootstrap,
                "ceac-ai-tools-campus-mcp",
                false,
                "ceac-campus-mcp",
                "Runtime MCP de CEAC AI Tools para CEAC Campus con sesion Moodle compartida desde navegador embebido."
        );
        properties.put("campus.base-url", CAMPUS_BASE_URL);
        properties.put("campus.login-url", CAMPUS_LOGIN_URL);
        properties.put("campus.dashboard-path", "/my/");
        properties.put("campus.http-timeout-seconds", "30");
        properties.put("campus.jcef-install-dir", projectRoot().resolve("jcef-bundle").resolve("campus"));
        properties.put("campus.jcef-cache-dir", projectRoot().resolve("jcef-cache").resolve("campus"));
        properties.put("campus.ui.enabled", "true");
        properties.put("campus.ui.show-logout", "true");
        properties.put("campus.ui.show-cookies", "true");
        properties.put("campus.ui.show-current-url", "true");
        properties.put("campus.ui.show-browser-view", "true");

        log("campus", "Arrancando runtime Campus en puerto local " + bootstrap.localPort() + ".");
        managedContext = startContext(CeacCampusRuntimeApplication.class, false, properties);

        try {
            embeddedPanel = managedContext.getBean(CampusEmbeddedPanel.class);
            waitForUrl(
                    localOAuthMetadataUrl(bootstrap),
                    90,
                    "No he podido arrancar Campus MCP en el tiempo esperado."
            );
            swaggerUrl = localSwaggerUrl(bootstrap);
            log("campus", "Campus MCP activo. Swagger: " + swaggerUrl);
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
            log("campus", "Parando runtime Campus.");
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
}


