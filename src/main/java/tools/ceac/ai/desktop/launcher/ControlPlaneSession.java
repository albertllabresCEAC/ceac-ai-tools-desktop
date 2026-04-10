package tools.ceac.ai.desktop.launcher;

import java.util.List;

/**
 * Local desktop session after authenticating with the control plane.
 *
 * <p>The session is global for the desktop shell, but it can carry bootstrap for several MCP
 * resources. In practice the {@code Login} tab fills this structure and the resource tabs read
 * their specific bootstrap from here.
 */
public record ControlPlaneSession(
        String controlPlaneBaseUrl,
        String accessToken,
        String externalUserId,
        String machineId,
        String clientVersion,
        String username,
        String email,
        BootstrapResponse bootstrap,
        List<ClientMcpResourceResponse> resources
) {
    /**
     * Returns whether the primary bootstrap of the session uses centralized auth.
     */
    public boolean usesCentralAuth() {
        return bootstrap != null && bootstrap.authExposureMode() == AuthExposureMode.CENTRAL_AUTH;
    }

    /**
     * Returns the bootstrap of the requested resource, or {@code null} when that resource is not
     * available to the authenticated desktop identity.
     */
    public BootstrapResponse bootstrapFor(String resourceKey) {
        if (resources != null) {
            BootstrapResponse resourceBootstrap = resources.stream()
                    .filter(resource -> resource.resourceKey().equalsIgnoreCase(resourceKey))
                    .map(ClientMcpResourceResponse::bootstrap)
                    .findFirst()
                    .orElse(null);
            if (resourceBootstrap != null) {
                return resourceBootstrap;
            }
        }
        // Backward compatibility with older control-plane deployments that still return only the
        // legacy top-level bootstrap. That payload always represents the Outlook runtime.
        if ("outlook".equalsIgnoreCase(resourceKey)) {
            return bootstrap;
        }
        return null;
    }
}

