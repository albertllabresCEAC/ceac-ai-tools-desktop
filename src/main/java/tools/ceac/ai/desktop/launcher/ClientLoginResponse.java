package tools.ceac.ai.desktop.launcher;

import java.time.Instant;
import java.util.List;
import tools.ceac.ai.security.ClientAccessLevel;
import tools.ceac.ai.security.ClientAccessLevelResolver;

/**
 * Result of desktop login against the control plane.
 *
 * <p>The response contains the desktop token, resolved identity and the list of MCP resources
 * currently available to the launcher.
 */
public record ClientLoginResponse(
        String tokenType,
        String accessToken,
        Instant expiresAt,
        String externalUserId,
        String username,
        String email,
        ClientAccessLevel accessLevel,
        BootstrapResponse bootstrap,
        List<ClientMcpResourceResponse> resources
) {
    public ClientLoginResponse {
        accessLevel = ClientAccessLevelResolver.resolve(accessLevel, accessToken);
    }
}

