package tools.ceac.ai.modules.campus.interfaces.desktop;

import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Component;

/**
 * Mantiene la referencia opcional al panel embebido del runtime Campus para poder forzar el modo
 * login cuando la sesion caduca desde cualquier capa del runtime.
 */
@Component
public class CampusUiCoordinator {

    private final AtomicReference<CampusEmbeddedPanel> panelRef = new AtomicReference<>();

    public void register(CampusEmbeddedPanel panel) {
        panelRef.set(panel);
    }

    public void unregister(CampusEmbeddedPanel panel) {
        panelRef.compareAndSet(panel, null);
    }

    public void forceLoginMode(String reason) {
        CampusEmbeddedPanel panel = panelRef.get();
        if (panel != null) {
            panel.forceLoginMode(reason);
        }
    }
}


