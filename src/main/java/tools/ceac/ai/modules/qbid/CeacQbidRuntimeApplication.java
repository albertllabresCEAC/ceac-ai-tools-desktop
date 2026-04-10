package tools.ceac.ai.modules.qbid;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import tools.ceac.ai.modules.qbid.config.McpRemoteProperties;

/**
 * Embedded Spring Boot runtime for the qBid resource inside CEAC AI Tools.
 *
 * <p>It does not expose a standalone end-user entry point. The desktop launcher creates and
 * destroys this context when the operator starts or stops the {@code QBid MCP} tab.
 */
@SpringBootApplication
@ConfigurationPropertiesScan(basePackageClasses = McpRemoteProperties.class)
public class CeacQbidRuntimeApplication {
}


