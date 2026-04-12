package tools.ceac.ai.desktop.launcher;

import java.util.List;

/**
 * Local desktop session after authenticating with the control plane.
 *
 * <p>The session is global for the desktop shell, but it can carry bootstrap for several MCP
 * resources. In practice the {@code Login} tab fills this structure and the resource tabs read
 * their specific bootstrap and launcher-issued local API tokens from here.
 *
 * <p>The resource catalog is immutable for the lifetime of the session. If the control plane adds
 * a new resource or reprovisions one that was missing, the launcher must perform a new login to
 * refresh this structure.
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
        List<ClientMcpResourceResponse> resources,
        String launcherTokenIssuer,
        String launcherTokenSecret
) {
    /**
     * Returns whether the primary bootstrap of the session uses centralized auth for the public MCP
     * surface.
     */
    public boolean usesCentralAuth() {
        return bootstrap != null && bootstrap.authExposureMode() == AuthExposureMode.CENTRAL_AUTH;
    }

    /**
     * Returns the complete resource entry, including the local API token minted by the launcher
     * for that resource, or {@code null} when the current session does not carry that resource.
     */
    public ClientMcpResourceResponse resourceFor(String resourceKey) {
        if (resourceKey == null) {
            return null;
        }
        if (resources != null) {
            ClientMcpResourceResponse resource = resources.stream()
                    .filter(entry -> entry.resourceKey().equalsIgnoreCase(resourceKey))
                    .findFirst()
                    .orElse(null);
            if (resource != null) {
                return resource;
            }
        }
        if ("outlook".equalsIgnoreCase(resourceKey) && bootstrap != null) {
            return new ClientMcpResourceResponse("outlook", bootstrap.displayName(), bootstrap, null);
        }
        return null;
    }

    /**
     * Returns the bootstrap of the requested resource, or {@code null} when that resource is not
     * available to the authenticated desktop identity.
     */
    public BootstrapResponse bootstrapFor(String resourceKey) {
        ClientMcpResourceResponse resource = resourceFor(resourceKey);
        if (resource != null && resource.bootstrap() != null) {
            return resource.bootstrap();
        }
        // Backward compatibility with older control-plane deployments that still return only the
        // legacy top-level bootstrap. That payload always represents the Outlook runtime.
        if ("outlook".equalsIgnoreCase(resourceKey)) {
            return bootstrap;
        }
        return null;
    }
}

