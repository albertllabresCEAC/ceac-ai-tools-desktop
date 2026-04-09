package tools.ceac.ai.desktop.launcher;

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
