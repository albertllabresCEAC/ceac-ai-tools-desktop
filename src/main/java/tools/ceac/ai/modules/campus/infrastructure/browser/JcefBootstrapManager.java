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
 * Process-wide singleton wrapper around JCEF initialization.
 */
@Component
public class JcefBootstrapManager {
    private static final Object LOCK = new Object();
    private static volatile CefApp sharedCefApp;

    private final CampusProperties properties;

    public JcefBootstrapManager(CampusProperties properties) {
        this.properties = properties;
    }

    public CefApp getApp() {
        CefApp app = sharedCefApp;
        if (app != null) {
            return app;
        }
        synchronized (LOCK) {
            if (sharedCefApp != null) {
                return sharedCefApp;
            }
            CefApp.CefAppState state = CefApp.getState();
            if (state == CefApp.CefAppState.INITIALIZED || state == CefApp.CefAppState.INITIALIZING) {
                sharedCefApp = CefApp.getInstance();
                return sharedCefApp;
            }
            if (state == CefApp.CefAppState.SHUTTING_DOWN) {
                throw new IllegalStateException("jcef_shutting_down");
            }
            if (state == CefApp.CefAppState.TERMINATED || state == CefApp.CefAppState.INITIALIZATION_FAILED) {
                throw new IllegalStateException("jcef_unavailable_after_shutdown");
            }
            sharedCefApp = buildApp();
            return sharedCefApp;
        }
    }

    private CefApp buildApp() {
        try {
            Files.createDirectories(properties.jcefInstallDir());
            Files.createDirectories(properties.jcefCacheDir());

            CefAppBuilder builder = new CefAppBuilder();
            builder.setInstallDir(properties.jcefInstallDir().toFile());
            if (canAttachAppHandler(CefApp.getState())) {
                builder.setAppHandler(new MavenCefAppHandlerAdapter() {
                });
            }

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

    private boolean canAttachAppHandler(CefApp.CefAppState state) {
        return state == CefApp.CefAppState.NONE || state == CefApp.CefAppState.NEW;
    }
}


