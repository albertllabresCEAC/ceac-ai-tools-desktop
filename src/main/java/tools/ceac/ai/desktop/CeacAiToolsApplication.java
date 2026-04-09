package tools.ceac.ai.desktop;

import java.awt.GraphicsEnvironment;
import tools.ceac.ai.desktop.ui.LauncherWindow;

/**
 * Punto de entrada de la shell desktop de CEAC AI Tools.
 *
 * <p>La shell no es un modulo MCP. Su unica responsabilidad es abrir la interfaz del producto y
 * orquestar sesiones, tuneles y runtimes locales.
 */
public final class CeacAiToolsApplication {

    private CeacAiToolsApplication() {
    }

    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless()) {
            throw new IllegalStateException("La shell desktop requiere entorno grafico. Usa los runtimes MCP especificos en modo headless.");
        }
        new LauncherWindow().show();
    }
}
