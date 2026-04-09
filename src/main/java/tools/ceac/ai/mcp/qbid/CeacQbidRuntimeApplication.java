package tools.ceac.ai.mcp.qbid;

import tools.ceac.ai.mcp.qbid.config.McpRemoteProperties;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Runtime Spring Boot embebible para el recurso qBid dentro de CEAC IA Tools.
 *
 * <p>No expone punto de entrada propio para usuario final. El launcher principal crea y destruye
 * este contexto cuando el operador arranca o detiene la pestaña {@code QBid MCP}.
 */
@SpringBootApplication
@ConfigurationPropertiesScan(basePackageClasses = McpRemoteProperties.class)
public class CeacQbidRuntimeApplication {
}
