package tools.ceac.ai.mcp.campus.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Component
public class McpUrlService {

    private static final String PROTECTED_RESOURCE_METADATA_PATH = "/.well-known/oauth-protected-resource";

    private final McpRemoteProperties properties;

    public McpUrlService(McpRemoteProperties properties) {
        this.properties = properties;
    }

    public String resolveBaseUrl(HttpServletRequest request) {
        if (StringUtils.hasText(properties.getPublicBaseUrl())) {
            return trimTrailingSlash(properties.getPublicBaseUrl().trim());
        }
        return trimTrailingSlash(ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(null)
                .replaceQuery(null)
                .build()
                .toUriString());
    }

    public String resolveMcpEndpointPath() {
        if (!StringUtils.hasText(properties.getMcpEndpoint())) {
            return "/mcp";
        }
        String endpoint = properties.getMcpEndpoint().trim();
        return endpoint.startsWith("/") ? endpoint : "/" + endpoint;
    }

    public String resolveMcpEndpointUrl(HttpServletRequest request) {
        return resolveBaseUrl(request) + resolveMcpEndpointPath();
    }

    public String resolveProtectedResourceMetadataUrl(HttpServletRequest request) {
        return resolveBaseUrl(request) + PROTECTED_RESOURCE_METADATA_PATH;
    }

    public String resolveRequiredAudience() {
        return properties.getAuth().getRequiredAudience() == null
                ? null
                : properties.getAuth().getRequiredAudience().trim();
    }

    private String trimTrailingSlash(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
