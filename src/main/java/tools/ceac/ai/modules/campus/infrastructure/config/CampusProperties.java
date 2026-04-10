package tools.ceac.ai.modules.campus.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

/**
 * Configurable properties for campus integration and local API behavior.
 */
@ConfigurationProperties(prefix = "campus")
public record CampusProperties(
        String baseUrl,
        String loginUrl,
        String dashboardPath,
        int httpTimeoutSeconds,
        Path jcefInstallDir,
        Path jcefCacheDir,
        Ui ui
) {
    public record Ui(
            boolean enabled,
            boolean showLogout,
            boolean showCookies,
            boolean showCurrentUrl,
            boolean showBrowserView
    ) {
    }

    public String dashboardUrl() {
        return baseUrl + dashboardPath;
    }
}


