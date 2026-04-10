package tools.ceac.ai.mcp.campus.web;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.ceac.ai.mcp.campus.config.McpRemoteProperties;
import tools.ceac.ai.mcp.campus.config.McpUrlService;

@RestController
public class OAuthProtectedResourceMetadataController {

    private final McpRemoteProperties properties;
    private final McpUrlService mcpUrlService;

    public OAuthProtectedResourceMetadataController(McpRemoteProperties properties, McpUrlService mcpUrlService) {
        this.properties = properties;
        this.mcpUrlService = mcpUrlService;
    }

    @GetMapping("/.well-known/oauth-protected-resource")
    public Map<String, Object> metadata(HttpServletRequest request) {
        return Map.of(
                "resource", mcpUrlService.resolveMcpEndpointUrl(request),
                "authorization_servers", List.of(properties.getAuth().getIssuerUri().trim()),
                "scopes_supported", List.of(properties.getAuth().getRequiredScope().trim()),
                "resource_name", properties.getAuth().getResourceName().trim()
        );
    }
}
