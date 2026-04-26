package tools.ceac.ai.modules.campus.interfaces.mcp;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.ceac.ai.security.AccessAwareMethodToolCallbackProvider;
import tools.ceac.ai.security.RuntimeAccessProperties;
import tools.ceac.ai.security.ToolAccessSupport;

@Configuration
public class CampusMcpConfig {

    @Bean
    public ToolCallbackProvider campusToolCallbackProvider(CampusMcpTools tools, RuntimeAccessProperties accessProperties) {
        return AccessAwareMethodToolCallbackProvider.builder()
                .toolObjects(tools)
                .methodFilter(method -> ToolAccessSupport.isMethodAllowed(method, accessProperties.getAccessLevel()))
                .build();
    }
}

