package com.alber.outlookdesktop.config;

import com.alber.outlookdesktop.mcp.OutlookMcpTools;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;

@Configuration
public class McpConfig {

    @Bean
    ToolCallbackProvider toolCallbackProvider(OutlookMcpTools outlookMcpTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(outlookMcpTools)
                .build();
    }
}
