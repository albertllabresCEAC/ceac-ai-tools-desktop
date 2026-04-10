package tools.ceac.ai.mcp.campus.interfaces.desktop;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PreDestroy;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefLoadHandlerAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import tools.ceac.ai.mcp.campus.application.service.CampusSessionService;
import tools.ceac.ai.mcp.campus.infrastructure.browser.JcefBootstrapManager;
import tools.ceac.ai.mcp.campus.infrastructure.campus.HttpResponseEvent;
import tools.ceac.ai.mcp.campus.infrastructure.config.CampusProperties;

/**
 * Panel Swing embebible que encapsula el login JCEF del Campus y la consola de actividad del
 * runtime.
 *
 * <p>Preserva el flujo original del proyecto Campus: el usuario inicia sesion en el navegador
 * incrustado y, cuando la cookie queda disponible, el runtime copia la sesion al cliente HTTP Java
 * para reutilizarla en REST y MCP.
 */
@Component
@ConditionalOnExpression("${campus.ui.enabled:true} and !${spring.main.headless:true}")
public class CampusEmbeddedPanel extends JPanel {

    private static final String CARD_LOGIN = "login";
    private static final String CARD_LOGS = "logs";
    private static final String CARD_CODE = "code";
    private static final Color MATRIX_BG = Color.BLACK;
    private static final Color MATRIX_GREEN = new Color(0, 255, 70);
    private static final Color MATRIX_DIM = new Color(0, 140, 40);
    private static final Font MATRIX_FONT = new Font("Courier New", Font.PLAIN, 13);

    private final CampusProperties properties;
    private final CampusSessionService sessionService;
    private final CampusUiCoordinator coordinator;
    private final JTextArea outputArea = new JTextArea();
    private final JTextArea codeArea = new JTextArea();
    private final JLabel statusLabel = new JLabel("Estado: esperando login");
    private final java.awt.CardLayout cardLayout = new java.awt.CardLayout();
    private final JPanel centerPanel = new JPanel(cardLayout);
    private final JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
    private final ObjectMapper objectMapper;
    private final int localPort;
    private final CefClient client;
    private final CefBrowser browser;

    private volatile boolean authenticated = false;
    private JComboBox<String> viewCombo;
    private JTextField urlField;

    public CampusEmbeddedPanel(CampusProperties properties,
                               CampusSessionService sessionService,
                               CampusUiCoordinator coordinator,
                               JcefBootstrapManager bootstrapManager,
                               ObjectMapper objectMapper,
                               @Value("${server.port}") int localPort) {
        super(new BorderLayout());
        this.properties = properties;
        this.sessionService = sessionService;
        this.coordinator = coordinator;
        this.objectMapper = objectMapper;
        this.localPort = localPort;
        this.client = bootstrapManager.getApp().createClient();
        this.browser = client.createBrowser(properties.baseUrl(), false, false);
        this.coordinator.register(this);

        setBackground(MATRIX_BG);
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setBackground(MATRIX_BG);
        outputArea.setForeground(MATRIX_GREEN);
        outputArea.setCaretColor(MATRIX_GREEN);
        outputArea.setFont(MATRIX_FONT);
        outputArea.setBorder(new EmptyBorder(6, 8, 6, 8));

        codeArea.setEditable(false);
        codeArea.setLineWrap(false);
        codeArea.setBackground(MATRIX_BG);
        codeArea.setForeground(MATRIX_GREEN);
        codeArea.setCaretColor(MATRIX_GREEN);
        codeArea.setFont(MATRIX_FONT);
        codeArea.setBorder(new EmptyBorder(6, 8, 6, 8));

        JLabel apiLabel = new JLabel("API local disponible en http://localhost:" + localPort + "/api");
        apiLabel.setForeground(MATRIX_DIM);
        apiLabel.setFont(MATRIX_FONT);
        apiLabel.setBorder(new EmptyBorder(2, 8, 4, 8));

        statusLabel.setForeground(MATRIX_GREEN);
        statusLabel.setFont(MATRIX_FONT.deriveFont(Font.BOLD));
        statusLabel.setBorder(new EmptyBorder(6, 8, 2, 8));

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(MATRIX_BG);
        top.setBorder(new LineBorder(MATRIX_DIM, 1, false));
        top.add(statusLabel, BorderLayout.NORTH);
        top.add(apiLabel, BorderLayout.SOUTH);

        JScrollPane logsScroll = new JScrollPane(outputArea);
        logsScroll.setBackground(MATRIX_BG);
        logsScroll.getViewport().setBackground(MATRIX_BG);
        logsScroll.setBorder(new LineBorder(MATRIX_DIM, 1));

        JScrollPane codeScroll = new JScrollPane(codeArea);
        codeScroll.setBackground(MATRIX_BG);
        codeScroll.getViewport().setBackground(MATRIX_BG);
        codeScroll.setBorder(new LineBorder(MATRIX_DIM, 1));

        centerPanel.setBackground(MATRIX_BG);
        centerPanel.add((java.awt.Component) browser.getUIComponent(), CARD_LOGIN);
        centerPanel.add(logsScroll, CARD_LOGS);
        centerPanel.add(codeScroll, CARD_CODE);

        add(top, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(toolbarPanel, BorderLayout.SOUTH);

        buildToolbar();
        registerHandlers();
        statusLabel.setText("Estado: comprobando sesion...");
        cardLayout.show(centerPanel, CARD_LOGIN);
    }

    public JComponent asComponent() {
        return this;
    }

    public boolean isAuthenticated() {
        return authenticated && sessionService.isAuthenticated();
    }

    public void forceLoginMode(String reason) {
        SwingUtilities.invokeLater(() -> showLoginMode(reason));
    }

    public void resetSession() {
        authenticated = false;
        sessionService.clearSession();
        forceLoginMode("Sesion reiniciada.");
    }

    @EventListener
    public void onHttpResponse(HttpResponseEvent event) {
        append("HTTP " + event.method() + " " + event.url());
        if (event.requestBody() != null) {
            append("  Body: " + event.requestBody());
        }
        SwingUtilities.invokeLater(() -> {
            if (urlField != null) {
                urlField.setText(event.url());
            }
        });

        String rawBody = event.body();
        SwingUtilities.invokeLater(() -> {
            codeArea.setText(rawBody);
            codeArea.setCaretPosition(0);
        });

        String content = prepareForBrowser(rawBody);
        String dataUrl = "data:text/html;base64,"
                + Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8));
        SwingUtilities.invokeLater(() -> browser.loadURL(dataUrl));
    }

    @PreDestroy
    public void destroy() {
        coordinator.unregister(this);
        try {
            browser.close(true);
        } catch (Exception ignored) {
        }
        try {
            client.dispose();
        } catch (Exception ignored) {
        }
    }

    private void buildToolbar() {
        toolbarPanel.setBackground(MATRIX_BG);
        toolbarPanel.setBorder(new LineBorder(MATRIX_DIM, 1, false));
        toolbarPanel.setVisible(false);

        JButton swaggerButton = matrixButton("[ SWAGGER UI ]");
        swaggerButton.addActionListener(event -> {
            try {
                java.awt.Desktop.getDesktop().browse(new URI("http://localhost:" + localPort + "/swagger-ui/index.html"));
            } catch (Exception ex) {
                append("Error al abrir Swagger: " + ex.getMessage());
            }
        });
        toolbarPanel.add(swaggerButton);

        if (properties.ui().showLogout()) {
            JButton logoutButton = matrixButton("[ CERRAR SESION ]");
            logoutButton.addActionListener(event -> {
                authenticated = false;
                sessionService.clearSession();
                showLoginMode("Sesion cerrada manualmente.");
            });
            toolbarPanel.add(logoutButton);
        }

        if (properties.ui().showCookies()) {
            JButton cookiesButton = matrixButton("[ MOSTRAR COOKIES ]");
            cookiesButton.addActionListener(event ->
                    append("--- Cookies ---\n" + sessionService.debugCookies() + "---------------"));
            toolbarPanel.add(cookiesButton);
        }

        if (properties.ui().showCurrentUrl()) {
            JLabel urlLabel = new JLabel("URL Actual:");
            urlLabel.setForeground(MATRIX_DIM);
            urlLabel.setFont(MATRIX_FONT);
            urlField = new JTextField(properties.dashboardUrl(), 40);
            urlField.setEditable(false);
            urlField.setBackground(MATRIX_BG);
            urlField.setForeground(MATRIX_GREEN);
            urlField.setCaretColor(MATRIX_GREEN);
            urlField.setFont(MATRIX_FONT);
            urlField.setBorder(new LineBorder(MATRIX_DIM));
            toolbarPanel.add(urlLabel);
            toolbarPanel.add(urlField);
        }

        if (properties.ui().showBrowserView()) {
            JLabel viewLabel = new JLabel("VISTA:");
            viewLabel.setForeground(MATRIX_DIM);
            viewLabel.setFont(MATRIX_FONT);

            viewCombo = new JComboBox<>(new String[]{"LOGS", "NAVEGADOR", "CODIGO"});
            viewCombo.setBackground(MATRIX_BG);
            viewCombo.setForeground(MATRIX_GREEN);
            viewCombo.setFont(MATRIX_FONT);
            viewCombo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public java.awt.Component getListCellRendererComponent(JList<?> list,
                                                                       Object value,
                                                                       int index,
                                                                       boolean isSelected,
                                                                       boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    setBackground(isSelected ? MATRIX_DIM : MATRIX_BG);
                    setForeground(MATRIX_GREEN);
                    setFont(MATRIX_FONT);
                    return this;
                }
            });
            viewCombo.addActionListener(event -> {
                String selected = (String) viewCombo.getSelectedItem();
                if ("NAVEGADOR".equals(selected)) {
                    cardLayout.show(centerPanel, CARD_LOGIN);
                } else if ("CODIGO".equals(selected)) {
                    cardLayout.show(centerPanel, CARD_CODE);
                } else {
                    cardLayout.show(centerPanel, CARD_LOGS);
                }
            });
            toolbarPanel.add(viewLabel);
            toolbarPanel.add(viewCombo);
        }
    }

    private JButton matrixButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(MATRIX_BG);
        button.setForeground(MATRIX_GREEN);
        button.setFont(MATRIX_FONT);
        button.setBorder(new LineBorder(MATRIX_DIM));
        button.setFocusPainted(false);
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        return button;
    }

    private void registerHandlers() {
        client.addLoadHandler(new CefLoadHandlerAdapter() {
            @Override
            public void onLoadEnd(CefBrowser browser, CefFrame frame, int httpStatusCode) {
                String currentUrl = browser.getURL();
                SwingUtilities.invokeLater(() -> {
                    if (currentUrl != null && !currentUrl.startsWith("data:")) {
                        append("Navegando: " + currentUrl + " (HTTP " + httpStatusCode + ")");
                        if (urlField != null) {
                            urlField.setText(currentUrl);
                        }
                    }
                    if (!authenticated && looksAuthenticatedByUrl(currentUrl)) {
                        tryAutoSyncAfterLogin();
                    }
                });
            }
        });
    }

    private boolean looksAuthenticatedByUrl(String url) {
        return url != null
                && url.startsWith(properties.baseUrl())
                && !url.equalsIgnoreCase(properties.loginUrl())
                && !url.contains("/login/index.php");
    }

    private void tryAutoSyncAfterLogin() {
        sessionService.syncFromEmbeddedBrowser();
        boolean hasSession = sessionService.isAuthenticated();
        append("Sincronizacion automatica de cookies.");
        append("Cookie MoodleSession detectada: " + (hasSession ? "SI" : "NO"));

        if (hasSession) {
            authenticated = true;
            browser.getSource(sessionService::storeSesskeyFromHtml);
            showLogsMode("Sesion iniciada. Ya puedes usar la API REST y el MCP.");
        } else {
            showLoginMode("Login incompleto. Continua en el navegador.");
        }
    }

    private void showLoginMode(String message) {
        authenticated = false;
        toolbarPanel.setVisible(false);
        statusLabel.setText("Estado: login requerido");
        append(message);
        cardLayout.show(centerPanel, CARD_LOGIN);
        String currentUrl = browser.getURL();
        if (currentUrl == null || !currentUrl.contains("/login/index.php")) {
            browser.loadURL(properties.loginUrl());
        }
    }

    private void showLogsMode(String message) {
        if (viewCombo != null) {
            viewCombo.setSelectedItem("LOGS");
        }
        toolbarPanel.setVisible(true);
        statusLabel.setText("Estado: autenticado");
        append(message);
        append("Swagger UI: http://localhost:" + localPort + "/swagger-ui/index.html");
        cardLayout.show(centerPanel, CARD_LOGS);
    }

    private String prepareForBrowser(String body) {
        String trimmed = body.trim();
        if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            try {
                JsonNode tree = objectMapper.readTree(trimmed);
                tree = expandNestedJson(tree);
                String pretty = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tree);
                String escaped = pretty
                        .replace("&", "&amp;")
                        .replace("<", "&lt;")
                        .replace(">", "&gt;");
                return "<html><body style='background:#111;color:#0f0;font-family:monospace;font-size:13px;padding:12px'>"
                        + "<pre>" + escaped + "</pre></body></html>";
            } catch (Exception ignored) {
            }
        }
        return body;
    }

    private JsonNode expandNestedJson(JsonNode node) {
        if (node.isTextual()) {
            String text = node.asText().trim();
            if (text.startsWith("{") || text.startsWith("[")) {
                try {
                    return expandNestedJson(objectMapper.readTree(text));
                } catch (Exception ignored) {
                }
            }
            return node;
        }
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            objectNode.fieldNames().forEachRemaining(key -> objectNode.set(key, expandNestedJson(objectNode.get(key))));
            return objectNode;
        }
        if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            for (int i = 0; i < arrayNode.size(); i++) {
                arrayNode.set(i, expandNestedJson(arrayNode.get(i)));
            }
            return arrayNode;
        }
        return node;
    }

    private void append(String text) {
        if (SwingUtilities.isEventDispatchThread()) {
            outputArea.append(text + System.lineSeparator());
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
            return;
        }
        SwingUtilities.invokeLater(() -> {
            outputArea.append(text + System.lineSeparator());
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        });
    }
}
