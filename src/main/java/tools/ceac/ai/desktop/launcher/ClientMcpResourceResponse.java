package tools.ceac.ai.desktop.launcher;

/**
 * Resumen del bootstrap de un recurso MCP disponible para el launcher desktop.
 */
public record ClientMcpResourceResponse(
        String resourceKey,
        String displayName,
        BootstrapResponse bootstrap
) {
}

