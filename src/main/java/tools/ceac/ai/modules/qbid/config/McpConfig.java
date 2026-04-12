package tools.ceac.ai.modules.qbid.config;

import tools.ceac.ai.modules.qbid.interfaces.mcp.QbidMcpTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra las tools de qBid en el servidor MCP.
 *
 * <p>El runtime actual usa transporte {@code STATELESS HTTP} sobre el endpoint MCP comun para
 * evitar dependencia de sesiones server-side y mejorar la interoperabilidad con clientes MCP
 * externos como ChatGPT o Claude. La autoconfiguracion de Spring AI levanta el endpoint usando
 * las properties definidas en {@code application.properties}.
 */
@Configuration
public class McpConfig {

    @Bean
    public ToolCallbackProvider qbidToolCallbacks(QbidMcpTools tools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(tools)
                .build();
    }
}



