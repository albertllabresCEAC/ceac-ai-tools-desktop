package tools.ceac.ai.mcp.outlook.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Resuelve las URLs publicas y locales que el recurso MCP debe anunciar.
 *
 * <p>Centralizar estas construcciones evita inconsistencias entre:
 *
 * <ul>
 *   <li>el endpoint MCP real</li>
 *   <li>la metadata `oauth-protected-resource`</li>
 *   <li>la audience exigida en tokens</li>
 * </ul>
 */
@Component
public class McpUrlService {

    private static final String PROTECTED_RESOURCE_METADATA_PATH = "/.well-known/oauth-protected-resource";

    private final McpRemoteProperties properties;

    public McpUrlService(McpRemoteProperties properties) {
        this.properties = properties;
    }

    /**
     * Devuelve la base URL publica efectiva del recurso.
     */
    public String resolveBaseUrl(HttpServletRequest request) {
        if (StringUtils.hasText(properties.getPublicBaseUrl())) {
            return trimTrailingSlash(properties.getPublicBaseUrl());
        }
        return trimTrailingSlash(ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(null)
                .replaceQuery(null)
                .build()
                .toUriString());
    }

    /**
     * Devuelve la ruta HTTP del endpoint MCP local.
     */
    public String resolveMcpEndpointPath() {
        String endpoint = properties.getMcpEndpoint();
        if (!StringUtils.hasText(endpoint)) {
            return "/mcp";
        }
        return endpoint.startsWith("/") ? endpoint : "/" + endpoint;
    }

    /**
     * Devuelve la URL publica completa del endpoint MCP.
     */
    public String resolveMcpEndpointUrl(HttpServletRequest request) {
        return resolveBaseUrl(request) + resolveMcpEndpointPath();
    }

    /**
     * Devuelve la URL publica de metadata OAuth del recurso protegido.
     */
    public String resolveProtectedResourceMetadataUrl(HttpServletRequest request) {
        return resolveBaseUrl(request) + PROTECTED_RESOURCE_METADATA_PATH;
    }

    /**
     * Devuelve la audience exigida por el recurso, derivada de configuracion o de la URL publica.
     */
    public String resolveRequiredAudience() {
        if (StringUtils.hasText(properties.getAuth().getRequiredAudience())) {
            return properties.getAuth().getRequiredAudience().trim();
        }
        return trimTrailingSlash(properties.getPublicBaseUrl()) + resolveMcpEndpointPath();
    }

    /**
     * Devuelve el issuer configurado para el recurso protegido.
     */
    public String resolveIssuerUri() {
        return properties.getAuth().getIssuerUri();
    }

    private String trimTrailingSlash(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
