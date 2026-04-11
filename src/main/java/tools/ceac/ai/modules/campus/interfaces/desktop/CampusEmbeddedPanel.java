package tools.ceac.ai.modules.campus.interfaces.desktop;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PreDestroy;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefLoadHandlerAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import tools.ceac.ai.modules.campus.application.service.CampusSessionService;
import tools.ceac.ai.modules.campus.infrastructure.browser.JcefBootstrapManager;
import tools.ceac.ai.modules.campus.infrastructure.campus.HttpResponseEvent;
import tools.ceac.ai.modules.campus.infrastructure.config.CampusProperties;

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

    public interface SessionListener {
        void onLoginRequired(String message);
        void onAuthenticationSuccess(String message);
    }

    private static final String CARD_LOGIN = "login";
    private static final String CARD_LOGS = "logs";
    private static final String CARD_CODE = "code";
    private static final String CARD_COOKIES = "cookies";
    private static final Color SURFACE_SOFT = new Color(231, 236, 245);
    private static final Color BORDER = new Color(204, 213, 226);
    private static final Color INK = new Color(15, 23, 42);
    private static final Color MUTED = new Color(118, 129, 150);
    private static final Color ACCENT = new Color(47, 107, 255);
    private static final Color ACCENT_SOFT = new Color(216, 228, 255);
    private static final Font LABEL_FONT = new Font("Bahnschrift SemiBold", Font.BOLD, 14);
    private static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font MONO_FONT = new Font("Consolas", Font.PLAIN, 12);

    private final CampusProperties properties;
    private final CampusSessionService sessionService;
    private final CampusUiCoordinator coordinator;
    private final JTextArea outputArea = new JTextArea();
    private final JTextArea codeArea = new JTextArea();
    private final JTextArea cookiesArea = new JTextArea();
    private final JLabel statusLabel = new JLabel("Estado: esperando login");
    private final java.awt.CardLayout cardLayout = new java.awt.CardLayout();
    private final JPanel centerPanel = new JPanel(cardLayout);
    private final JPanel toolbarPanel = new JPanel(new BorderLayout(12, 0));
    private final ObjectMapper objectMapper;
    private final int localPort;
    private final CefClient client;
    private final CefBrowser browser;

    private volatile boolean authenticated = false;
    private volatile SessionListener sessionListener;
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

        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder());

        configureConsoleArea(outputArea, true);
        configureConsoleArea(codeArea, false);
        configureConsoleArea(cookiesArea, true);

        statusLabel.setForeground(INK);
        statusLabel.setFont(LABEL_FONT);
        statusLabel.setBorder(new EmptyBorder(0, 0, 0, 0));

        JScrollPane logsScroll = buildScrollPane(outputArea);
        JScrollPane codeScroll = buildScrollPane(codeArea);
        JScrollPane cookiesScroll = buildScrollPane(cookiesArea);

        centerPanel.setOpaque(false);
        centerPanel.add((java.awt.Component) browser.getUIComponent(), CARD_LOGIN);
        centerPanel.add(logsScroll, CARD_LOGS);
        centerPanel.add(codeScroll, CARD_CODE);
        centerPanel.add(cookiesScroll, CARD_COOKIES);

        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.setOpaque(false);
        top.setBorder(new EmptyBorder(0, 0, 10, 0));
        top.add(statusLabel, BorderLayout.NORTH);
        top.add(toolbarPanel, BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

        buildToolbar();
        registerHandlers();
        refreshCookiesView();
        statusLabel.setText("Estado: comprobando sesion...");
        cardLayout.show(centerPanel, CARD_LOGIN);
    }

    public JComponent asComponent() {
        return this;
    }

    public boolean isAuthenticated() {
        return authenticated && sessionService.isAuthenticated();
    }

    public void setSessionListener(SessionListener sessionListener) {
        this.sessionListener = sessionListener;
    }

    public void forceLoginMode(String reason) {
        SwingUtilities.invokeLater(() -> showLoginMode(reason));
    }

    public void resetSession() {
        authenticated = false;
        sessionService.clearSession();
        refreshCookiesView();
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
        toolbarPanel.setOpaque(false);

        JPanel urlPanel = new JPanel(new BorderLayout(8, 0));
        urlPanel.setOpaque(false);
        urlPanel.add(sectionLabel("URL actual"), BorderLayout.WEST);
        urlField = new JTextField(properties.loginUrl(), 40);
        urlField.setEditable(false);
        styleReadonlyField(urlField);
        urlPanel.add(urlField, BorderLayout.CENTER);
        toolbarPanel.add(urlPanel, BorderLayout.CENTER);

        viewCombo = new JComboBox<>();
        DefaultComboBoxModel<String> viewModel = new DefaultComboBoxModel<>(new String[]{"NAVEGADOR", "LOGS", "CODIGO"});
        if (properties.ui().showCookies()) {
            viewModel.addElement("COOKIES");
        }
        viewCombo.setModel(viewModel);
        styleCombo(viewCombo);
        viewCombo.addActionListener(event -> showSelectedView((String) viewCombo.getSelectedItem()));

        JPanel viewPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        viewPanel.setOpaque(false);
        viewPanel.add(sectionLabel("Vista"));
        viewPanel.add(viewCombo);
        toolbarPanel.add(viewPanel, BorderLayout.EAST);
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
        refreshCookiesView();
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
        refreshCookiesView();
        statusLabel.setText("Estado: login requerido");
        append(message);
        if (viewCombo != null) {
            viewCombo.setSelectedItem("NAVEGADOR");
        }
        cardLayout.show(centerPanel, CARD_LOGIN);
        String currentUrl = browser.getURL();
        if (currentUrl == null || !currentUrl.contains("/login/index.php")) {
            browser.loadURL(properties.loginUrl());
        }
        notifyLoginRequired(message);
    }

    private void showLogsMode(String message) {
        if (viewCombo != null) {
            viewCombo.setSelectedItem("LOGS");
        }
        statusLabel.setText("Estado: autenticado");
        append(message);
        append("Swagger UI: http://localhost:" + localPort + "/swagger-ui/index.html");
        cardLayout.show(centerPanel, CARD_LOGS);
        notifyAuthenticationSuccess(message);
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
                return "<html><body style='background:#F2F4F8;color:#0F172A;font-family:Consolas,monospace;font-size:13px;padding:12px'>"
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

    private void configureConsoleArea(JTextArea area, boolean wrap) {
        area.setEditable(false);
        area.setLineWrap(wrap);
        area.setWrapStyleWord(wrap);
        area.setBackground(SURFACE_SOFT);
        area.setForeground(INK);
        area.setCaretColor(ACCENT);
        area.setFont(MONO_FONT);
        area.setBorder(new EmptyBorder(10, 12, 10, 12));
    }

    private JScrollPane buildScrollPane(JTextArea area) {
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder()
        ));
        scroll.getViewport().setBackground(SURFACE_SOFT);
        scroll.setBackground(SURFACE_SOFT);
        return scroll;
    }

    private JLabel sectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(LABEL_FONT);
        label.setForeground(MUTED);
        return label;
    }

    private void styleReadonlyField(JTextField field) {
        field.setFont(BODY_FONT);
        field.setForeground(INK);
        field.setBackground(SURFACE_SOFT);
        field.setCaretColor(ACCENT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
    }

    private void styleCombo(JComboBox<String> combo) {
        combo.setFont(BODY_FONT);
        combo.setBackground(SURFACE_SOFT);
        combo.setForeground(INK);
        combo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(3, 6, 3, 6)
        ));
        combo.setFocusable(false);
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> list,
                                                                   Object value,
                                                                   int index,
                                                                   boolean isSelected,
                                                                   boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? ACCENT_SOFT : Color.WHITE);
                setForeground(INK);
                setFont(BODY_FONT);
                return this;
            }
        });
        UIManager.put("ComboBox.selectionBackground", ACCENT_SOFT);
        UIManager.put("ComboBox.selectionForeground", INK);
    }

    private void showSelectedView(String selected) {
        if ("CODIGO".equals(selected)) {
            cardLayout.show(centerPanel, CARD_CODE);
            return;
        }
        if ("COOKIES".equals(selected)) {
            refreshCookiesView();
            cardLayout.show(centerPanel, CARD_COOKIES);
            return;
        }
        if ("LOGS".equals(selected)) {
            cardLayout.show(centerPanel, CARD_LOGS);
            return;
        }
        cardLayout.show(centerPanel, CARD_LOGIN);
    }

    private void refreshCookiesView() {
        String cookies = sessionService.debugCookies();
        String content = cookies == null || cookies.isBlank()
                ? "No hay cookies sincronizadas."
                : cookies;
        SwingUtilities.invokeLater(() -> {
            cookiesArea.setText(content);
            cookiesArea.setCaretPosition(0);
        });
    }

    private void notifyLoginRequired(String message) {
        SessionListener listener = sessionListener;
        if (listener != null) {
            listener.onLoginRequired(message);
        }
    }

    private void notifyAuthenticationSuccess(String message) {
        SessionListener listener = sessionListener;
        if (listener != null) {
            listener.onAuthenticationSuccess(message);
        }
    }
}


