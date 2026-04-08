package com.alber.outlookdesktop.ui;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AppRuntimeBridge implements ApplicationListener<WebServerInitializedEvent> {

    private final String swaggerPath;

    public AppRuntimeBridge(@Value("${springdoc.swagger-ui.path:/swagger-ui/index.html}") String swaggerPath) {
        this.swaggerPath = swaggerPath;
        LauncherEvents.publish(new AppRuntimeState("Starting", null));
    }

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        String swaggerUrl = "http://localhost:" + event.getWebServer().getPort() + swaggerPath;
        LauncherEvents.publish(new AppRuntimeState("Running", swaggerUrl));
        GuiLogPublisher.publish("[app] Application running. Swagger: " + swaggerUrl + System.lineSeparator());
    }

    @EventListener
    public void onContextClosed(ContextClosedEvent ignored) {
        LauncherEvents.publish(new AppRuntimeState("Stopped", null));
    }

    @PreDestroy
    void destroy() {
        LauncherEvents.publish(new AppRuntimeState("Stopped", null));
    }
}
