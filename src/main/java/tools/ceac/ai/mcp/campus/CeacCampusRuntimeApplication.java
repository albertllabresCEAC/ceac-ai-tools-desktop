package tools.ceac.ai.mcp.campus;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Embedded Spring runtime for Campus MCP inside CEAC AI Tools Desktop.
 *
 * <p>It does not open its own top-level window. The desktop shell retrieves the required
 * components from this context and mounts them inside the {@code Campus MCP} tab.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class CeacCampusRuntimeApplication {
}
