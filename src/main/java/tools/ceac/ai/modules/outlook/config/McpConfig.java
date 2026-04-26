package tools.ceac.ai.modules.outlook.config;

import tools.ceac.ai.modules.outlook.interfaces.mcp.OutlookMcpTools;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.tool.ToolCallbackProvider;
import tools.ceac.ai.security.AccessAwareMethodToolCallbackProvider;
import tools.ceac.ai.security.RuntimeAccessProperties;
import tools.ceac.ai.security.ToolAccessSupport;

@Configuration
public class McpConfig {

    @Bean
    ToolCallbackProvider toolCallbackProvider(OutlookMcpTools outlookMcpTools, RuntimeAccessProperties accessProperties) {
        return AccessAwareMethodToolCallbackProvider.builder()
                .toolObjects(outlookMcpTools)
                .methodFilter(method -> ToolAccessSupport.isMethodAllowed(method, accessProperties.getAccessLevel()))
                .build();
    }
}


