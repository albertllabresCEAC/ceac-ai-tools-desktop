package com.alber.outlookdesktop.launcher;

/**
 * Recursos MCP que el launcher sabe operar de forma nativa.
 *
 * <p>Cada entrada fija tres datos operativos locales:
 *
 * <ul>
 *   <li>el {@code resourceKey} con el que habla con el control plane</li>
 *   <li>el puerto de metrics reservado para {@code cloudflared}</li>
 *   <li>el puerto local por defecto del runtime asociado</li>
 * </ul>
 */
public enum ManagedMcpKind {
    OUTLOOK("outlook", "Outlook MCP", 20241, 8080),
    QBID("qbid", "QBid MCP", 20242, 8082);

    private final String resourceKey;
    private final String displayName;
    private final int metricsPort;
    private final int fallbackLocalPort;

    ManagedMcpKind(String resourceKey, String displayName, int metricsPort, int fallbackLocalPort) {
        this.resourceKey = resourceKey;
        this.displayName = displayName;
        this.metricsPort = metricsPort;
        this.fallbackLocalPort = fallbackLocalPort;
    }

    public String resourceKey() {
        return resourceKey;
    }

    public String displayName() {
        return displayName;
    }

    public int metricsPort() {
        return metricsPort;
    }

    public int fallbackLocalPort() {
        return fallbackLocalPort;
    }
}
