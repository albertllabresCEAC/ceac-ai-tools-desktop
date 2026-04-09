package tools.ceac.ai.desktop.launcher;

import java.time.Instant;
import java.util.List;

public record ClientLoginResponse(
        String tokenType,
        String accessToken,
        Instant expiresAt,
        String externalUserId,
        String username,
        String email,
        BootstrapResponse bootstrap,
        List<ClientMcpResourceResponse> resources
) {
}
