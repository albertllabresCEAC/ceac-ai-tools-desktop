package tools.ceac.ai.desktop.launcher;

import java.util.List;

/**
 * Estado local de sesion del launcher despues de autenticar con el control plane.
 *
 * <p>La sesion es global para el desktop, pero puede contener bootstrap para varios recursos MCP.
 * En la practica, la pestana {@code Login} rellena esta estructura y las pestanas {@code Outlook
 * MCP} y {@code QBid MCP} leen de aqui su bootstrap correspondiente.
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
     * Indica si el bootstrap principal de la sesion usa auth centralizada.
     */
    public boolean usesCentralAuth() {
        return bootstrap != null && bootstrap.authExposureMode() == AuthExposureMode.CENTRAL_AUTH;
    }

    /**
     * Devuelve el bootstrap del recurso indicado, o {@code null} si ese recurso no esta disponible
     * para el usuario autenticado.
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
