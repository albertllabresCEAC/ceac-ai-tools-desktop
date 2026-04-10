package tools.ceac.ai.modules.campus.interfaces.mcp;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CampusMcpConfig {

    @Bean
    public ToolCallbackProvider campusToolCallbackProvider(CampusMcpTools tools) {
        return MethodToolCallbackProvider.builder().toolObjects(tools).build();
    }
}

