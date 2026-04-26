package tools.ceac.ai.modules.campus;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import tools.ceac.ai.security.RuntimeAccessProperties;

/**
 * Embedded Spring runtime for Campus MCP inside CEAC AI Tools Desktop.
 *
 * <p>It does not open its own top-level window. The desktop shell retrieves the required
 * components from this context and shows them inside the Campus login modal when authentication is
 * required.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableConfigurationProperties(RuntimeAccessProperties.class)
public class CeacCampusRuntimeApplication {
}


