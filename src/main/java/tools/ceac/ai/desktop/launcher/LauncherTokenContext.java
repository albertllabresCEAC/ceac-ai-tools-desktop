package tools.ceac.ai.desktop.launcher;

/**
 * Shared signing context used by the launcher to mint local API tokens per resource.
 */
public record LauncherTokenContext(
        String issuerUri,
        String sharedSecret
) {
}
