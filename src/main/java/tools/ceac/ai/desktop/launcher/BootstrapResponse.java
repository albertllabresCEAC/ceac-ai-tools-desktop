package tools.ceac.ai.desktop.launcher;

/**
 * Bootstrap payload consumed by the desktop launcher for one MCP resource.
 *
 * <p>This is the desktop-side mirror of the control-plane bootstrap contract. It carries both the
 * public OAuth metadata and the local runtime settings required to start a resource.
 */
public record BootstrapResponse(
        String resourceKey,
        String displayName,
        int localPort,
        String tunnelId,
        String tunnelToken,
        String mcpHostname,
        String authHostname,
        String mcpPublicBaseUrl,
        String authPublicBaseUrl,
        String issuerUri,
        String jwkSetUri,
        String requiredAudience,
        String requiredScope,
        String resourceName,
        AuthExposureMode authExposureMode,
        boolean cloudflaredManagedRemotely
) {
}
