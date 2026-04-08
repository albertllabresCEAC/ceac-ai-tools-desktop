package com.alber.outlookdesktop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import com.alber.outlookdesktop.config.JacobProperties;
import com.alber.outlookdesktop.config.OutlookProperties;
import com.alber.outlookdesktop.ui.LauncherWindow;

import java.awt.GraphicsEnvironment;

/**
 * Punto de entrada de la aplicacion desktop.
 *
 * <p>En entorno grafico se abre el launcher Swing y no se arranca directamente Spring Boot.
 * En entorno headless se permite arrancar la aplicacion como servicio puro, lo que resulta util
 * para tests, automatizaciones o despliegues sin interfaz.
 */
@SpringBootApplication
@ConfigurationPropertiesScan(basePackageClasses = {OutlookProperties.class, JacobProperties.class})
public class OutlookDesktopComMcpApplication {

    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless()) {
            SpringApplication.run(OutlookDesktopComMcpApplication.class, args);
            return;
        }
        new LauncherWindow().show();
    }
}
