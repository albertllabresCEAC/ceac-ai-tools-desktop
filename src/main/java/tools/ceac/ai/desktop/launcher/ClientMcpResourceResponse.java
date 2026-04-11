package tools.ceac.ai.desktop.launcher;

/**
 * Resumen de un recurso MCP disponible para el launcher desktop.
 *
 * <p>El control plane aporta el bootstrap. El propio launcher enriquece esta estructura con un
 * token local por recurso para Swagger y para la API expuesta solo en {@code localhost}.
 */
public record ClientMcpResourceResponse(
        String resourceKey,
        String displayName,
        BootstrapResponse bootstrap,
        ResourceAccessTokenResponse resourceToken
) {
}

