package tools.ceac.ai.desktop.launcher;

import java.util.List;

/**
 * Session resources enriched with launcher-issued local API tokens.
 */
public record LauncherSessionResources(
        LauncherTokenContext tokenContext,
        List<ClientMcpResourceResponse> resources
) {
}
