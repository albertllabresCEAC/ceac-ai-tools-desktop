package tools.ceac.ai.mcp.qbid.config;

import tools.ceac.ai.mcp.qbid.mcp.QbidMcpTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra las tools de qBid en el servidor MCP.
 *
 * <p>El runtime actual usa transporte {@code STREAMABLE HTTP} para alinearse con el resto de
 * CEAC IA Tools y con clientes MCP externos como ChatGPT o Claude. La autoconfiguracion de Spring
 * AI levanta el endpoint usando las properties definidas en {@code application.properties}.
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

