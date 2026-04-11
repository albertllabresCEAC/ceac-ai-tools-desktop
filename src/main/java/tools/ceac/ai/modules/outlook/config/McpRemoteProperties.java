package tools.ceac.ai.modules.outlook.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Properties de exposicion remota del MCP local.
 *
 * <p>Estas properties no describen el control plane, sino el recurso protegido que publica la app
 * local una vez que el launcher ha materializado el bootstrap en `.env.generated`.
 */
@ConfigurationProperties(prefix = "mcp.remote")
public class McpRemoteProperties {

    private String publicBaseUrl = "http://localhost:8080";
    private String mcpEndpoint = "/mcp";
    private List<String> allowedOriginPatterns = new ArrayList<>(List.of(
            "http://localhost:*",
            "http://127.0.0.1:*",
            "https://localhost:*",
            "https://127.0.0.1:*"
    ));
    private final Auth auth = new Auth();
    private final Launcher launcher = new Launcher();

    public String getPublicBaseUrl() {
        return publicBaseUrl;
    }

    public void setPublicBaseUrl(String publicBaseUrl) {
        this.publicBaseUrl = publicBaseUrl;
    }

    public String getMcpEndpoint() {
        return mcpEndpoint;
    }

    public void setMcpEndpoint(String mcpEndpoint) {
        this.mcpEndpoint = mcpEndpoint;
    }

    public List<String> getAllowedOriginPatterns() {
        return allowedOriginPatterns;
    }

    public void setAllowedOriginPatterns(List<String> allowedOriginPatterns) {
        this.allowedOriginPatterns = sanitizeList(allowedOriginPatterns);
    }

    public Auth getAuth() {
        return auth;
    }

    public Launcher getLauncher() {
        return launcher;
    }

    public void setAllowedOrigins(String allowedOrigins) {
        this.allowedOriginPatterns = parseCsv(allowedOrigins, this.allowedOriginPatterns);
    }

    private static List<String> parseCsv(String csv, List<String> fallback) {
        if (csv == null || csv.isBlank()) {
            return fallback;
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static List<String> sanitizeList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return new ArrayList<>();
        }
        return values.stream()
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Subgrupo de properties OAuth del recurso protegido.
     */
    public static class Auth {

        private boolean enabled;
        private String issuerUri;
        private String jwkSetUri;
        private String requiredAudience;
        private String requiredScope = "mcp:tools";
        private String resourceName = "Outlook MCP";
        private List<String> scopesSupported = new ArrayList<>(List.of("mcp:tools"));

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getIssuerUri() {
            return issuerUri;
        }

        public void setIssuerUri(String issuerUri) {
            this.issuerUri = issuerUri;
        }

        public String getJwkSetUri() {
            return jwkSetUri;
        }

        public void setJwkSetUri(String jwkSetUri) {
            this.jwkSetUri = jwkSetUri;
        }

        public String getRequiredAudience() {
            return requiredAudience;
        }

        public void setRequiredAudience(String requiredAudience) {
            this.requiredAudience = requiredAudience;
        }

        public String getRequiredScope() {
            return requiredScope;
        }

        public void setRequiredScope(String requiredScope) {
            this.requiredScope = requiredScope;
        }

        public String getResourceName() {
            return resourceName;
        }

        public void setResourceName(String resourceName) {
            this.resourceName = resourceName;
        }

        public List<String> getScopesSupported() {
            return scopesSupported;
        }

        public void setScopesSupported(List<String> scopesSupported) {
            this.scopesSupported = scopesSupported;
        }
    }

    public static class Launcher {

        private boolean enabled;
        private String issuerUri;
        private String sharedSecret;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getIssuerUri() {
            return issuerUri;
        }

        public void setIssuerUri(String issuerUri) {
            this.issuerUri = issuerUri;
        }

        public String getSharedSecret() {
            return sharedSecret;
        }

        public void setSharedSecret(String sharedSecret) {
            this.sharedSecret = sharedSecret;
        }
    }
}


