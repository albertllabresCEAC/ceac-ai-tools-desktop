package tools.ceac.ai.modules.trello;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import tools.ceac.ai.modules.trello.config.McpRemoteProperties;

/**
 * Embedded Spring Boot runtime for the Trello resource inside CEAC AI Tools.
 */
@SpringBootApplication
@ConfigurationPropertiesScan(basePackageClasses = McpRemoteProperties.class)
public class CeacTrelloRuntimeApplication {
}
