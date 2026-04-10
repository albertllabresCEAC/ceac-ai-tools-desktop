package tools.ceac.ai.desktop;

import java.awt.GraphicsEnvironment;
import tools.ceac.ai.desktop.ui.CeacLauncherWindow;

/**
 * Punto de entrada de la shell desktop de CEAC AI Tools.
 *
 * <p>La shell no es un modulo funcional. Su responsabilidad es abrir la interfaz del producto y
 * orquestar la sesion desktop, los tuneles locales y los modulos {@code outlook}, {@code campus}
 * y {@code qbid}.
 */
public final class CeacAiToolsApplication {

    private CeacAiToolsApplication() {
    }

    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless()) {
            throw new IllegalStateException("La shell desktop requiere entorno grafico. Usa los runtimes MCP especificos en modo headless.");
        }
        new CeacLauncherWindow().show();
    }
}

