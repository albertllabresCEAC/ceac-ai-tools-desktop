package tools.ceac.ai.modules.campus.infrastructure.browser;

import tools.ceac.ai.modules.campus.infrastructure.config.CampusProperties;
import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.MavenCefAppHandlerAdapter;
import org.cef.CefApp;
import org.cef.CefSettings;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Lazy singleton wrapper around JCEF initialization.
 */
@Component
public class JcefBootstrapManager {
    private final CampusProperties properties;
    private volatile CefApp cefApp;

    public JcefBootstrapManager(CampusProperties properties) {
        this.properties = properties;
    }

    public CefApp getApp() {
        if (cefApp == null) {
            synchronized (JcefBootstrapManager.class) {
                if (cefApp == null) {
                    cefApp = buildApp();
                }
            }
        }
        return cefApp;
    }

    private CefApp buildApp() {
        try {
            Files.createDirectories(properties.jcefInstallDir());
            Files.createDirectories(properties.jcefCacheDir());

            CefAppBuilder builder = new CefAppBuilder();
            builder.setInstallDir(properties.jcefInstallDir().toFile());
            builder.setAppHandler(new MavenCefAppHandlerAdapter() {
            });

            CefSettings settings = builder.getCefSettings();
            settings.windowless_rendering_enabled = false;
            settings.persist_session_cookies = true;
            settings.cache_path = properties.jcefCacheDir().toAbsolutePath().toString();
            settings.root_cache_path = properties.jcefCacheDir().toAbsolutePath().toString();
            settings.log_file = new File("jcef.log").getAbsolutePath();

            builder.addJcefArgs("--disable-gpu");
            builder.addJcefArgs("--autoplay-policy=no-user-gesture-required");
            return builder.build();
        } catch (IOException e) {
            throw new IllegalStateException("cannot_create_jcef_dirs", e);
        } catch (Exception e) {
            throw new IllegalStateException("cannot_init_jcef", e);
        }
    }
}


