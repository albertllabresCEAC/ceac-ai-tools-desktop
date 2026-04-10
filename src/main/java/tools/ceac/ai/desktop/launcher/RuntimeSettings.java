package tools.ceac.ai.desktop.launcher;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reduced set of Spring properties derived from bootstrap and injected into a local runtime.
 *
 * <p>This record isolates the desktop shell from the details of each MCP runtime while keeping the
 * bootstrap-to-runtime mapping explicit and testable.
 */
public record RuntimeSettings(
        String mcpPublicBaseUrl,
        String authPublicBaseUrl,
        String issuerUri,
        String jwkSetUri,
        String requiredAudience,
        String requiredScope,
        String resourceName
) {

    /**
     * Converts the runtime settings to the Spring property map expected by the local runtime.
     */
    public Map<String, Object> toSpringProperties() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("MCP_PUBLIC_BASE_URL", mcpPublicBaseUrl);
        properties.put("MCP_AUTH_ENABLED", "true");
        properties.put("MCP_OAUTH_ISSUER_URI", issuerUri);
        properties.put("MCP_OAUTH_JWK_SET_URI", jwkSetUri);
        properties.put("MCP_OAUTH_REQUIRED_AUDIENCE", requiredAudience);
        properties.put("MCP_OAUTH_REQUIRED_SCOPE", requiredScope);
        properties.put("MCP_RESOURCE_NAME", resourceName);
        return properties;
    }
}
