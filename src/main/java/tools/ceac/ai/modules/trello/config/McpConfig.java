package tools.ceac.ai.modules.trello.config;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.ceac.ai.modules.trello.interfaces.mcp.TrelloMcpTools;
import tools.ceac.ai.security.AccessAwareMethodToolCallbackProvider;
import tools.ceac.ai.security.RuntimeAccessProperties;
import tools.ceac.ai.security.ToolAccessSupport;

@Configuration
public class McpConfig {

    @Bean
    public ToolCallbackProvider trelloToolCallbacks(TrelloMcpTools tools, RuntimeAccessProperties accessProperties) {
        return AccessAwareMethodToolCallbackProvider.builder()
                .toolObjects(tools)
                .methodFilter(method -> ToolAccessSupport.isMethodAllowed(method, accessProperties.getAccessLevel()))
                .build();
    }
}
