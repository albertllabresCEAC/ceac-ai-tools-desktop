package tools.ceac.ai.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Resolves the effective CEAC access level from the central login response or, as a fallback,
 * directly from the Keycloak JWT when older control-plane deployments do not expose the field yet.
 */
public final class ClientAccessLevelResolver {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private ClientAccessLevelResolver() {
    }

    public static ClientAccessLevel resolve(ClientAccessLevel declaredLevel, String accessToken) {
        if (declaredLevel != null) {
            return declaredLevel;
        }

        ClientAccessLevel fromToken = fromAccessToken(accessToken);
        return fromToken == null ? ClientAccessLevel.READ_WRITE : fromToken;
    }

    public static ClientAccessLevel fromAccessToken(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return null;
        }

        try {
            String[] parts = accessToken.split("\\.");
            if (parts.length < 2) {
                return null;
            }

            byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
            JsonNode payload = OBJECT_MAPPER.readTree(new String(payloadBytes, StandardCharsets.UTF_8));
            JsonNode roles = payload.path("realm_access").path("roles");
            if (!roles.isArray()) {
                return null;
            }

            boolean readOnly = false;
            for (JsonNode roleNode : roles) {
                String role = roleNode.asText();
                if ("CEAC_READ_WRITE".equals(role) || "CEAC_AI_USER".equals(role)) {
                    return ClientAccessLevel.READ_WRITE;
                }
                if ("CEAC_READ_ONLY".equals(role)) {
                    readOnly = true;
                }
            }
            return readOnly ? ClientAccessLevel.READ_ONLY : null;
        } catch (Exception exception) {
            return null;
        }
    }
}
