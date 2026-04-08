package com.alber.outlookdesktop.launcher;

public record BootstrapResponse(
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
