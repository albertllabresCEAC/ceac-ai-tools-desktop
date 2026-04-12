package tools.ceac.ai.desktop;

import java.awt.GraphicsEnvironment;
import tools.ceac.ai.desktop.ui.CeacLauncherWindow;

/**
 * Punto de entrada de la shell desktop de CEAC AI Tools.
 *
 * <p>La shell no es un modulo funcional. Su responsabilidad es abrir la interfaz del producto y
 * orquestar la sesion desktop, los tuneles locales, los tokens locales de API y los modulos
 * {@code outlook}, {@code campus}, {@code qbid} y {@code trello}.
 *
 * <p>Tambien separa explicitamente dos superficies:
 *
 * <ul>
 *   <li>el MCP publico que anuncia OAuth central a clientes externos</li>
 *   <li>la API local del operador en {@code 127.0.0.1}, protegida con tokens emitidos por el launcher</li>
 * </ul>
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

