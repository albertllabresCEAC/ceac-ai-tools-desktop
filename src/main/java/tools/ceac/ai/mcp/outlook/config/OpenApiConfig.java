package tools.ceac.ai.mcp.outlook.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI outlookOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Outlook Desktop COM API")
                .version("v1")
                .description("API REST para controlar Outlook Desktop mediante COM")
                .contact(new Contact().name("Local user")));
    }
}
