package tools.ceac.ai.modules.outlook.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI outlookOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Outlook Desktop COM API")
                        .version("v1")
                        .description("API REST para controlar Outlook Desktop mediante COM")
                        .contact(new Contact().name("Local user")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    @Bean
    OpenApiCustomizer outlookOpenApiCustomizer() {
        return openApi -> {
            if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
                return;
            }
            var messageQuery = openApi.getComponents().getSchemas().get("MessageQuery");
            if (messageQuery == null || messageQuery.getProperties() == null) {
                return;
            }
            Object property = messageQuery.getProperties().get("since");
            if (!(property instanceof Schema<?> since)) {
                return;
            }
            since.setExample(OffsetDateTime.now().minusDays(7).truncatedTo(ChronoUnit.SECONDS).toString());
        };
    }
}


