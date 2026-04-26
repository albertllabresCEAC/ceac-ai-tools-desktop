package tools.ceac.ai.modules.outlook;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import tools.ceac.ai.modules.outlook.config.JacobProperties;
import tools.ceac.ai.modules.outlook.config.OutlookProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import tools.ceac.ai.security.RuntimeAccessProperties;

/**
 * Runtime Spring Boot del modulo Outlook MCP.
 *
 * <p>La shell desktop crea este contexto cuando el usuario arranca el recurso Outlook. Mantener
 * este runtime separado de la UI evita que el modulo Outlook se mezcle con la orquestacion del
 * producto.
 */
@SpringBootApplication
@ConfigurationPropertiesScan(basePackageClasses = {OutlookProperties.class, JacobProperties.class})
@EnableConfigurationProperties(RuntimeAccessProperties.class)
public class OutlookMcpRuntimeApplication {
}


