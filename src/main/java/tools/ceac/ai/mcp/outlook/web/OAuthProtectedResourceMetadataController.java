package tools.ceac.ai.mcp.outlook.web;

import tools.ceac.ai.mcp.outlook.config.McpRemoteProperties;
import tools.ceac.ai.mcp.outlook.config.McpUrlService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Publica la metadata OAuth Protected Resource del MCP local.
 *
 * <p>Este endpoint es el punto de entrada que usan clientes MCP externos para descubrir:
 *
 * <ul>
 *   <li>que issuer deben usar</li>
 *   <li>que scopes soporta el recurso</li>
 *   <li>cual es la URL real del endpoint MCP</li>
 * </ul>
 */
@RestController
public class OAuthProtectedResourceMetadataController {

    private final McpRemoteProperties properties;
    private final McpUrlService mcpUrlService;

    public OAuthProtectedResourceMetadataController(McpRemoteProperties properties, McpUrlService mcpUrlService) {
        this.properties = properties;
        this.mcpUrlService = mcpUrlService;
    }

    /**
     * Devuelve el documento `oauth-protected-resource` del recurso MCP local.
     */
    @GetMapping("/.well-known/oauth-protected-resource")
    public OAuthProtectedResourceMetadataResponse getMetadata(HttpServletRequest request) {
        String issuerUri = properties.getAuth().getIssuerUri();
        return new OAuthProtectedResourceMetadataResponse(
                mcpUrlService.resolveMcpEndpointUrl(request),
                StringUtils.hasText(issuerUri) ? List.of(issuerUri) : List.of(),
                properties.getAuth().getScopesSupported(),
                List.of("header"),
                properties.getAuth().getResourceName()
        );
    }
}
