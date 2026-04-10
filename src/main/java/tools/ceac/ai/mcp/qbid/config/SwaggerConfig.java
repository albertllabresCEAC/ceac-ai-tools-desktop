package tools.ceac.ai.mcp.qbid.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CEAC AI Tools - QBid MCP")
                        .description("API REST y runtime MCP para qBID / sBID dentro de CEAC AI Tools")
                        .version("1.0.0"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    @Bean
    public OperationCustomizer removeAuthHeader() {
        return (operation, handlerMethod) -> {
            if (operation.getParameters() != null) {
                operation.getParameters().removeIf(p ->
                        "header".equals(p.getIn()) && "Authorization".equals(p.getName())
                );
            }
            return operation;
        };
    }
}
