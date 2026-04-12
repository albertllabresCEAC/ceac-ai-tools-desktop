package tools.ceac.ai.modules.trello.config;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.ceac.ai.modules.trello.interfaces.mcp.TrelloMcpTools;

@Configuration
public class McpConfig {

    @Bean
    public ToolCallbackProvider trelloToolCallbacks(TrelloMcpTools tools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(tools)
                .build();
    }
}
