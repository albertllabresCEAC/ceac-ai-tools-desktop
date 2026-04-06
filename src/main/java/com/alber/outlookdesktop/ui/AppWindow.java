package com.alber.outlookdesktop.ui;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.net.URI;
import java.util.function.Consumer;

@Component
public class AppWindow implements ApplicationListener<WebServerInitializedEvent> {

    private final Environment environment;
    private final String swaggerPath;
    private final Consumer<String> logConsumer = this::appendLog;

    private volatile JFrame frame;
    private volatile JLabel statusLabel;
    private volatile JLabel swaggerLabel;
    private volatile JTextArea logArea;

    public AppWindow(Environment environment,
                     @Value("${springdoc.swagger-ui.path:/swagger-ui.html}") String swaggerPath) {
        this.environment = environment;
        this.swaggerPath = swaggerPath;
        if (!GraphicsEnvironment.isHeadless()) {
            GuiLogPublisher.register(logConsumer);
            createWindow();
        }
    }

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        String host = "localhost";
        String swaggerUrl = "http://" + host + ":" + event.getWebServer().getPort() + swaggerPath;
        updateStatus("Running");
        updateSwagger(swaggerUrl);
        appendLog("Application running. Swagger: " + swaggerUrl + System.lineSeparator());
    }

    @EventListener
    public void onContextClosed(ContextClosedEvent ignored) {
        updateStatus("Stopped");
    }

    @PreDestroy
    void destroy() {
        GuiLogPublisher.unregister(logConsumer);
        if (frame != null) {
            SwingUtilities.invokeLater(() -> frame.dispose());
        }
    }

    private void createWindow() {
        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame(environment.getProperty("spring.application.name", "Outlook Desktop COM MCP"));
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setLayout(new BorderLayout(12, 12));

            JPanel topPanel = new JPanel();
            topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
            topPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));

            statusLabel = new JLabel("Status: Starting");
            swaggerLabel = new JLabel("Swagger: pending");

            JPanel buttonPanel = new JPanel();
            JButton openSwaggerButton = new JButton("Open Swagger");
            openSwaggerButton.addActionListener(event -> openSwagger());
            JButton copySwaggerButton = new JButton("Copy URL");
            copySwaggerButton.addActionListener(event -> copySwaggerUrl());
            buttonPanel.add(openSwaggerButton);
            buttonPanel.add(copySwaggerButton);

            topPanel.add(statusLabel);
            topPanel.add(swaggerLabel);
            topPanel.add(buttonPanel);

            logArea = new JTextArea();
            logArea.setEditable(false);
            logArea.setLineWrap(true);
            logArea.setWrapStyleWord(true);

            JScrollPane scrollPane = new JScrollPane(logArea);
            scrollPane.setBorder(BorderFactory.createTitledBorder("Logs"));

            window.add(topPanel, BorderLayout.NORTH);
            window.add(scrollPane, BorderLayout.CENTER);
            window.setSize(new Dimension(900, 520));
            window.setMinimumSize(new Dimension(700, 400));
            window.setLocationRelativeTo(null);
            window.setVisible(true);

            frame = window;
        });
    }

    private void updateStatus(String status) {
        if (statusLabel == null) {
            return;
        }
        SwingUtilities.invokeLater(() -> statusLabel.setText("Status: " + status));
    }

    private void updateSwagger(String swaggerUrl) {
        if (swaggerLabel == null) {
            return;
        }
        SwingUtilities.invokeLater(() -> swaggerLabel.setText("Swagger: " + swaggerUrl));
    }

    private void appendLog(String message) {
        if (logArea == null) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            logArea.append(message);
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void openSwagger() {
        String swaggerUrl = extractSwaggerUrl();
        if (swaggerUrl == null || !Desktop.isDesktopSupported()) {
            return;
        }
        try {
            Desktop.getDesktop().browse(URI.create(swaggerUrl));
        } catch (Exception ex) {
            appendLog("Unable to open Swagger in browser: " + ex.getMessage() + System.lineSeparator());
        }
    }

    private void copySwaggerUrl() {
        String swaggerUrl = extractSwaggerUrl();
        if (swaggerUrl == null) {
            return;
        }
        Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new StringSelection(swaggerUrl), null);
        appendLog("Swagger URL copied to clipboard." + System.lineSeparator());
    }

    private String extractSwaggerUrl() {
        if (swaggerLabel == null) {
            return null;
        }
        String text = swaggerLabel.getText();
        if (text == null || !text.startsWith("Swagger: http")) {
            return null;
        }
        return text.substring("Swagger: ".length()).trim();
    }
}
