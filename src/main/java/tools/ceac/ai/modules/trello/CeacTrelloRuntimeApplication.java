package tools.ceac.ai.modules.trello;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import tools.ceac.ai.modules.trello.config.McpRemoteProperties;
import tools.ceac.ai.security.RuntimeAccessProperties;

/**
 * Embedded Spring Boot runtime for the Trello resource inside CEAC AI Tools.
 *
 * <p>This runtime serves two surfaces:
 *
 * <ul>
 *   <li>the public MCP endpoint announced through control-plane bootstrap</li>
 *   <li>the local-only REST and Swagger surface used by the desktop operator</li>
 * </ul>
 *
 * <p>The Trello account token consumed by this runtime is captured locally by the launcher through
 * the browser authorization flow and is never returned by the control plane.
 */
@SpringBootApplication
@ConfigurationPropertiesScan(basePackageClasses = McpRemoteProperties.class)
@EnableConfigurationProperties(RuntimeAccessProperties.class)
public class CeacTrelloRuntimeApplication {
}
