package tools.ceac.ai.modules.outlook.interfaces.oauth;

import java.util.List;

/**
 * Representacion del documento `oauth-protected-resource` publicado por el MCP local.
 */
public record OAuthProtectedResourceMetadataResponse(
        String resource,
        List<String> authorization_servers,
        List<String> scopes_supported,
        List<String> bearer_methods_supported,
        String resource_name
) {
}



