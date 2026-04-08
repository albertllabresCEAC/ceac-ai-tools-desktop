package com.alber.outlookdesktop.launcher;

/**
 * Estado local de sesion del launcher despues de autenticar con el control plane.
 */
public record ControlPlaneSession(
        String controlPlaneBaseUrl,
        String accessToken,
        String externalUserId,
        String machineId,
        String clientVersion,
        String username,
        String email,
        BootstrapResponse bootstrap
) {
    /**
     * Indica si el bootstrap recibido corresponde al modo soportado por el launcher actual.
     */
    public boolean usesCentralAuth() {
        return bootstrap != null && bootstrap.authExposureMode() == AuthExposureMode.CENTRAL_AUTH;
    }
}
