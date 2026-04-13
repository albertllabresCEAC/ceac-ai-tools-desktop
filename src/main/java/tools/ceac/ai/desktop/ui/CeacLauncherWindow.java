package tools.ceac.ai.desktop.ui;

import tools.ceac.ai.modules.campus.interfaces.desktop.CampusEmbeddedPanel;
import tools.ceac.ai.desktop.launcher.AuthExposureMode;
import tools.ceac.ai.desktop.launcher.BootstrapResponse;
import tools.ceac.ai.desktop.launcher.CampusRuntimeService;
import tools.ceac.ai.desktop.launcher.ClientMcpResourceResponse;
import tools.ceac.ai.desktop.launcher.ClientLoginRequest;
import tools.ceac.ai.desktop.launcher.ClientLoginResponse;
import tools.ceac.ai.desktop.launcher.ControlPlaneSession;
import tools.ceac.ai.desktop.launcher.LauncherResourceTokenService;
import tools.ceac.ai.desktop.launcher.LauncherSessionResources;
import tools.ceac.ai.desktop.launcher.ManagedMcpKind;
import tools.ceac.ai.desktop.launcher.OutlookRuntimeService;
import tools.ceac.ai.desktop.launcher.QbidRuntimeService;
import tools.ceac.ai.desktop.launcher.RemoteLauncherService;
import tools.ceac.ai.desktop.launcher.ResourceAccessTokenResponse;
import tools.ceac.ai.desktop.launcher.RuntimeSettings;
import tools.ceac.ai.desktop.launcher.TrelloAuthorizationService;
import tools.ceac.ai.desktop.launcher.TrelloConnection;
import tools.ceac.ai.desktop.launcher.TrelloLocalTokenStore;
import tools.ceac.ai.desktop.launcher.TrelloRuntimeService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Paint;
import java.awt.Taskbar;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.text.JTextComponent;

/**
 * Main Swing window of CEAC AI Tools Desktop.
 *
 * <p>The window is the product shell. It is responsible for:
 *
 * <ul>
 *   <li>desktop login against the control plane</li>
 *   <li>projecting bootstrap data into the UI</li>
 *   <li>starting and stopping each managed resource</li>
 *   <li>projecting resource-specific UI such as the Campus JCEF login modal</li>
 * </ul>
 *
 * <p>It deliberately does not contain provisioning logic or direct Cloudflare management. Those
 * concerns stay in {@code RemoteLauncherService} and in the backend control plane.
 */
public class CeacLauncherWindow {

    private static final String DEFAULT_CONTROL_PLANE_URL = "https://control.dartmaker.com";
    private static final String DEFAULT_CLIENT_VERSION = "1.0.0";
    private static final String DEFAULT_TRELLO_API_KEY = "c0f93f8f62cbcffb3e515327fb293fbf";
    private static final Color BLUE_DARK = new Color(30, 47, 151);
    private static final Color BLUE_MAIN = new Color(47, 107, 255);
    private static final Color CYAN = new Color(0, 209, 255);
    private static final Color VIOLET = new Color(123, 63, 228);
    private static final Color PINK = new Color(255, 79, 216);
    private static final Color WHITE = Color.WHITE;
    private static final Color GRAY_LIGHT = new Color(242, 244, 248);
    private static final Color GRAY_MEDIUM = new Color(160, 167, 184);
    private static final Color BLACK_SOFT = new Color(15, 23, 42);

    private static final Color APP_BG = BLUE_DARK;
    private static final Color APP_BG_DEEP = BLUE_MAIN;
    private static final Color SURFACE = GRAY_LIGHT;
    private static final Color SURFACE_SOFT = new Color(231, 236, 245);
    private static final Color SURFACE_WASH = new Color(221, 228, 239);
    private static final Color BORDER = new Color(204, 213, 226);
    private static final Color INK = BLACK_SOFT;
    private static final Color MUTED = GRAY_MEDIUM;
    private static final Color ACCENT = BLUE_MAIN;
    private static final Color ACCENT_DARK = BLUE_DARK;
    private static final Color ACCENT_SOFT = new Color(216, 228, 255);
    private static final Color OUTLOOK_ACCENT = BLUE_MAIN;
    private static final Color QBID_ACCENT = VIOLET;
    private static final Color CAMPUS_ACCENT = CYAN;
    private static final Color TRELLO_ACCENT = new Color(0, 121, 191);
    private static final Color LOG_BG = BLACK_SOFT;
    private static final Color LOG_FG = GRAY_LIGHT;
    private static final Color OK = CYAN;
    private static final Color WARN = VIOLET;
    private static final Color ERR = new Color(255, 98, 135);

    private static final Font DISPLAY_FONT = new Font("Bahnschrift SemiBold", Font.BOLD, 30);
    private static final Font BANNER_FONT = new Font("Bahnschrift SemiBold", Font.BOLD, 24);
    private static final Font HEADING_FONT = new Font("Bahnschrift SemiBold", Font.BOLD, 18);
    private static final Font LABEL_FONT = new Font("Segoe UI Semibold", Font.PLAIN, 13);
    private static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font CHIP_FONT = new Font("Segoe UI Semibold", Font.PLAIN, 12);
    private static final Font MONO_FONT = new Font("Consolas", Font.PLAIN, 12);

    private final RemoteLauncherService remoteLauncherService = new RemoteLauncherService();
    private final LauncherResourceTokenService launcherResourceTokenService = new LauncherResourceTokenService();
    private final OutlookRuntimeService outlookRuntimeService = new OutlookRuntimeService();
    private final QbidRuntimeService qbidRuntimeService = new QbidRuntimeService();
    private final CampusRuntimeService campusRuntimeService = new CampusRuntimeService();
    private final TrelloRuntimeService trelloRuntimeService = new TrelloRuntimeService();
    private final TrelloAuthorizationService trelloAuthorizationService = new TrelloAuthorizationService();
    private final TrelloLocalTokenStore trelloLocalTokenStore = new TrelloLocalTokenStore();
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final Consumer<String> logConsumer = this::appendLog;
    private final boolean developmentMode = Boolean.parseBoolean(System.getenv().getOrDefault("DARTMAKER_DEV_MODE", "false"));
    private final String trelloApiKey = System.getenv().getOrDefault("TRELLO_API_KEY", DEFAULT_TRELLO_API_KEY);

    private JFrame frame;
    private JTextArea logArea;
    private JTextField controlPlaneUrlField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField machineIdField;
    private JTextField clientVersionField;
    private JLabel loginStatus;
    private JLabel loginUser;
    private JLabel loginResources;
    private JLabel sessionBadge;
    private JButton loginButton;
    private JButton logoutButton;

    private ResourceWidgets outlookWidgets;
    private ResourceWidgets qbidWidgets;
    private ResourceWidgets campusWidgets;
    private ResourceWidgets trelloWidgets;
    private JTextField qbidUserField;
    private JPasswordField qbidPasswordField;
    private JDialog campusLoginDialog;
    private volatile boolean campusDialogInternalClose;
    private Image tabsLogoImage;

    private ControlPlaneSession controlPlaneSession;
    private TrelloConnection trelloConnection;

    public void show() {
        GuiLogPublisher.register(logConsumer);
        SwingUtilities.invokeLater(this::createWindow);
    }

    private void createWindow() {
        frame = new JFrame(developmentMode ? "CEAC AI Tools [DEV]" : "CEAC AI Tools");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        tabsLogoImage = loadTabsLogoImage();
        List<Image> applicationIcons = loadApplicationIcons();
        if (!applicationIcons.isEmpty()) {
            frame.setIconImages(applicationIcons);
            if (Taskbar.isTaskbarSupported()) {
                try {
                    Taskbar.getTaskbar().setIconImage(applicationIcons.get(0));
                } catch (Exception ignored) {
                }
            }
        }
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { shutdown(); }
        });

        GradientPanel root = new GradientPanel();
        root.setLayout(new BorderLayout(18, 18));
        root.setBorder(new EmptyBorder(18, 18, 18, 18));
        frame.setContentPane(root);

        JTabbedPane tabs = buildTabs();
        SurfacePanel tabsPanel = new SurfacePanel(new Color(BLACK_SOFT.getRed(), BLACK_SOFT.getGreen(), BLACK_SOFT.getBlue(), 86), 34, new Color(BLUE_MAIN.getRed(), BLUE_MAIN.getGreen(), BLUE_MAIN.getBlue(), 95));
        tabsPanel.setLayout(new BorderLayout());
        tabsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        tabsPanel.add(tabs, BorderLayout.CENTER);
        tabsPanel.setMinimumSize(new Dimension(0, 420));

        JPanel activityPanel = buildActivityPanel();
        activityPanel.setPreferredSize(new Dimension(0, 320));
        activityPanel.setMinimumSize(new Dimension(0, 260));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tabsPanel, activityPanel);
        split.setBorder(null);
        split.setOpaque(false);
        split.setResizeWeight(0.0);
        split.setContinuousLayout(true);
        split.setDividerSize(12);
        root.add(split, BorderLayout.CENTER);

        loadDefaults();
        resetLogin();
        resetResource(outlookWidgets);
        resetResource(qbidWidgets);
        resetResource(campusWidgets);
        resetResource(trelloWidgets);
        frame.setMinimumSize(new Dimension(1180, 840));
        frame.setSize(1380, 920);
        frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
        SwingUtilities.invokeLater(() -> {
            int availableHeight = split.getHeight();
            int dividerSize = split.getDividerSize();
            int topMinimum = tabsPanel.getMinimumSize().height;
            int bottomMinimum = activityPanel.getMinimumSize().height;
            int topPreferred = Math.max(topMinimum, tabsPanel.getPreferredSize().height);
            int maxTop = Math.max(topMinimum, availableHeight - bottomMinimum - dividerSize);
            split.setDividerLocation(Math.min(topPreferred, maxTop));
        });
    }

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(LABEL_FONT.deriveFont(14f));
        tabs.setForeground(INK);
        tabs.setOpaque(false);
        tabs.setBorder(BorderFactory.createEmptyBorder());
        tabs.setUI(new DashboardTabsUI(tabsLogoImage));
        installTabHandCursor(tabs);
        tabs.addTab("Login", buildLoginTab());
        tabs.addTab(moduleName(ManagedMcpKind.OUTLOOK), buildResourceTab(ManagedMcpKind.OUTLOOK));
        tabs.addTab(moduleName(ManagedMcpKind.QBID), buildResourceTab(ManagedMcpKind.QBID));
        tabs.addTab(moduleName(ManagedMcpKind.CAMPUS), buildResourceTab(ManagedMcpKind.CAMPUS));
        tabs.addTab(moduleName(ManagedMcpKind.TRELLO), buildResourceTab(ManagedMcpKind.TRELLO));
        tabs.addChangeListener(event -> handleTabSelection(tabs.getSelectedIndex()));
        SwingUtilities.invokeLater(() -> handleTabSelection(tabs.getSelectedIndex()));
        return tabs;
    }

    private void installTabHandCursor(JTabbedPane tabs) {
        MouseAdapter cursorHandler = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent event) {
                updateTabCursor(tabs, event);
            }

            @Override
            public void mouseEntered(MouseEvent event) {
                updateTabCursor(tabs, event);
            }

            @Override
            public void mouseExited(MouseEvent event) {
                tabs.setCursor(Cursor.getDefaultCursor());
            }
        };
        tabs.addMouseMotionListener(cursorHandler);
        tabs.addMouseListener(cursorHandler);
    }

    private void updateTabCursor(JTabbedPane tabs, MouseEvent event) {
        int tabIndex = tabs.indexAtLocation(event.getX(), event.getY());
        tabs.setCursor(Cursor.getPredefinedCursor(tabIndex >= 0 ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
    }

    private JPanel buildLoginTab() {
        JPanel panel = contentPanel();
        JPanel body = new JPanel(new GridLayout(1, 2, 12, 12));
        body.setOpaque(false);
        body.add(buildLoginAccessCard());
        JPanel right = new JPanel(new BorderLayout(0, 12));
        right.setOpaque(false);
        right.add(buildLoginSummaryCard(), BorderLayout.CENTER);
        body.add(right);
        panel.add(body, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildLoginAccessCard() {
        JPanel card = card("Panel de control", "Acceso principal", ACCENT);
        JPanel form = formPanel();
        controlPlaneUrlField = new JTextField(DEFAULT_CONTROL_PLANE_URL, 28);
        controlPlaneUrlField.setEditable(developmentMode);
        usernameField = new JTextField(28);
        passwordField = new JPasswordField(28);
        machineIdField = new JTextField(28);
        machineIdField.setEditable(false);
        clientVersionField = new JTextField(DEFAULT_CLIENT_VERSION, 28);
        clientVersionField.setEditable(false);

        styleInput(controlPlaneUrlField, developmentMode);
        styleInput(usernameField, true);
        styleInput(passwordField, true);
        styleInput(machineIdField, false);
        styleInput(clientVersionField, false);

        int row = 0;
        if (developmentMode) {
            addRow(form, row++, "Control plane URL", controlPlaneUrlField);
        }
        addRow(form, row++, "Usuario o email", usernameField);
        addRow(form, row++, "Password", passwordField);
        addRow(form, row++, "Machine ID", machineIdField);
        addRow(form, row, "Client version", clientVersionField);
        JPanel footer = new JPanel(new BorderLayout(0, 10));
        footer.setOpaque(false);
        JPanel buttons = buttonRow();
        loginButton = new JButton("Iniciar sesion");
        logoutButton = new JButton("Cerrar sesion");
        stylePrimaryButton(loginButton, ACCENT);
        styleDangerButton(logoutButton);
        loginButton.addActionListener(e -> runAsync(this::login));
        logoutButton.addActionListener(e -> logout());
        usernameField.addActionListener(e -> triggerLogin());
        passwordField.addActionListener(e -> triggerLogin());
        controlPlaneUrlField.addActionListener(e -> triggerLogin());
        buttons.add(loginButton);
        buttons.add(logoutButton);
        footer.add(buttons, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildLoginSummaryCard() {
        JPanel card = card("Sesion", "Estado actual", ACCENT_DARK);
        JPanel form = formPanel();
        loginStatus = valueLabel("Sesion: no autenticada");
        loginUser = valueLabel("pending");
        loginResources = valueLabel("pending");
        addRow(form, 0, "Estado", loginStatus);
        addRow(form, 1, "Usuario", loginUser);
        addRow(form, 2, "Recursos", loginResources);
        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildResourceTab(ManagedMcpKind kind) {
        if (kind == ManagedMcpKind.CAMPUS) {
            return buildCampusTab();
        }
        JPanel panel = contentPanel();
        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);

        if (kind == ManagedMcpKind.OUTLOOK) {
            outlookWidgets = new ResourceWidgets(kind);
            JPanel grid = new JPanel(new GridLayout(1, 2, 12, 12));
            grid.setOpaque(false);
            grid.add(buildOutlookAccessCard(outlookWidgets));
            grid.add(buildResourceRuntimeCard(outlookWidgets));
            body.add(grid, BorderLayout.CENTER);
        } else if (kind == ManagedMcpKind.QBID) {
            qbidWidgets = new ResourceWidgets(kind);
            JPanel grid = new JPanel(new GridLayout(1, 2, 12, 12));
            grid.setOpaque(false);
            grid.add(buildQbidAccessCard(qbidWidgets));
            grid.add(buildResourceRuntimeCard(qbidWidgets));
            body.add(grid, BorderLayout.CENTER);
        } else {
            trelloWidgets = new ResourceWidgets(kind);
            JPanel grid = new JPanel(new GridLayout(1, 2, 12, 12));
            grid.setOpaque(false);
            grid.add(buildTrelloAccessCard(trelloWidgets));
            grid.add(buildResourceRuntimeCard(trelloWidgets));
            body.add(grid, BorderLayout.CENTER);
        }
        panel.add(body, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildCampusTab() {
        JPanel panel = contentPanel();
        campusWidgets = new ResourceWidgets(ManagedMcpKind.CAMPUS);
        JPanel grid = new JPanel(new GridLayout(1, 2, 12, 12));
        grid.setOpaque(false);
        grid.add(buildCampusAccessCard(campusWidgets));
        grid.add(buildResourceRuntimeCard(campusWidgets));

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.add(grid, BorderLayout.CENTER);
        panel.add(body, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildOutlookAccessCard(ResourceWidgets widgets) {
        JPanel card = card("Acceso " + moduleName(widgets.kind), "Operacion local", accentFor(widgets.kind));
        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(buildParagraph("Outlook bot no necesita credenciales porque usa el Outlook del escritorio del usuario en su version classic. Debe estar instalado y configurado con la cuenta correspondiente.", 300), BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout(0, 8));
        footer.setOpaque(false);
        footer.add(buildModuleControls(widgets, false), BorderLayout.NORTH);
        widgets.accessRequirement = accessRequirementLabel(300);
        footer.add(widgets.accessRequirement, BorderLayout.SOUTH);
        body.add(footer, BorderLayout.SOUTH);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildQbidAccessCard(ResourceWidgets widgets) {
        JPanel card = card("Acceso " + moduleName(widgets.kind), "Operacion local", accentFor(widgets.kind));
        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);

        JPanel form = formPanel();
        qbidUserField = new JTextField(28);
        qbidPasswordField = new JPasswordField(28);
        styleInput(qbidUserField, true);
        styleInput(qbidPasswordField, true);
        qbidUserField.addActionListener(e -> triggerQbidStart());
        qbidPasswordField.addActionListener(e -> triggerQbidStart());
        addRow(form, 0, "Usuario qBid", qbidUserField);
        addRow(form, 1, "Password qBid", qbidPasswordField);
        body.add(form, BorderLayout.NORTH);
        JPanel footer = new JPanel(new BorderLayout(0, 8));
        footer.setOpaque(false);
        footer.add(buildModuleControls(widgets, false), BorderLayout.NORTH);
        widgets.accessRequirement = accessRequirementLabel(300);
        footer.add(widgets.accessRequirement, BorderLayout.SOUTH);
        body.add(footer, BorderLayout.SOUTH);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildCampusAccessCard(ResourceWidgets widgets) {
        JPanel card = card("Acceso " + moduleName(widgets.kind), "Operacion local", accentFor(widgets.kind));
        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(buildParagraph("El acceso se realiza por via de explorador una vez arrancado el modulo.", 300), BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout(0, 8));
        footer.setOpaque(false);
        footer.add(buildModuleControls(widgets, true), BorderLayout.NORTH);
        widgets.accessRequirement = accessRequirementLabel(300);
        footer.add(widgets.accessRequirement, BorderLayout.SOUTH);
        body.add(footer, BorderLayout.SOUTH);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildTrelloAccessCard(ResourceWidgets widgets) {
        JPanel card = card("Acceso " + moduleName(widgets.kind), "Operacion local", accentFor(widgets.kind));
        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(buildParagraph("Conecta tu cuenta Trello desde el navegador para que el modulo pueda operar tableros, listas y tarjetas con tu usuario.", 300), BorderLayout.NORTH);

        JPanel form = formPanel();
        widgets.connectionState = valueLabel("Trello: no conectado");
        widgets.connectedAccount = readonly();
        addRow(form, 0, "Conexion Trello", widgets.connectionState);
        addRow(form, 1, "Cuenta", widgets.connectedAccount);
        body.add(form, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(0, 8));
        footer.setOpaque(false);

        JPanel actions = new JPanel();
        actions.setOpaque(false);
        actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));

        JPanel connectionButtons = buttonRow();
        widgets.connect = new JButton("Conectar Trello");
        widgets.reconnect = new JButton("Reconectar");
        widgets.disconnect = new JButton("Desconectar");
        styleSecondaryButton(widgets.connect, accentFor(widgets.kind));
        styleSecondaryButton(widgets.reconnect, accentFor(widgets.kind));
        styleNeutralButton(widgets.disconnect);
        widgets.connect.addActionListener(e -> runAsync(this::connectTrello));
        widgets.reconnect.addActionListener(e -> runAsync(this::reconnectTrello));
        widgets.disconnect.addActionListener(e -> runAsync(this::disconnectTrello));
        connectionButtons.add(widgets.connect);
        connectionButtons.add(widgets.reconnect);
        connectionButtons.add(widgets.disconnect);
        actions.add(connectionButtons);
        actions.add(Box.createVerticalStrut(8));
        actions.add(buildModuleControls(widgets, false));

        footer.add(actions, BorderLayout.NORTH);
        widgets.accessRequirement = accessRequirementLabel(300);
        footer.add(widgets.accessRequirement, BorderLayout.SOUTH);
        body.add(footer, BorderLayout.SOUTH);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildResourceBootstrapCard(ResourceWidgets widgets) {
        JPanel card = card("Bootstrap " + moduleName(widgets.kind), "Datos remotos", accentFor(widgets.kind));
        JPanel form = formPanel();
        widgets.tunnelId = valueLabel("pending");
        widgets.host = valueLabel("pending");
        widgets.issuer = readonly();
        widgets.jwk = readonly();
        widgets.audience = readonly();
        widgets.scope = readonly();
        widgets.resourceName = readonly();
        addRow(form, 0, "Tunnel ID", widgets.tunnelId);
        addRow(form, 1, "Host", widgets.host);
        addRow(form, 2, "Issuer", widgets.issuer);
        addRow(form, 3, "JWKS", widgets.jwk);
        addRow(form, 4, "Audience", widgets.audience);
        addRow(form, 5, "Scope", widgets.scope);
        addRow(form, 6, "Resource name", widgets.resourceName);
        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildResourceRuntimeCard(ResourceWidgets widgets) {
        JPanel card = card("Estado " + moduleName(widgets.kind), "Telemetria local", accentFor(widgets.kind));
        JPanel form = formPanel();
        widgets.prereq = valueLabel("Prerequisites: pending");
        widgets.tunnel = valueLabel("Tunnel: detenido");
        widgets.app = valueLabel("Runtime: detenido");
        widgets.apiUrl = buildUrlWidget(accentFor(widgets.kind));
        widgets.mcpUrl = buildUrlWidget(accentFor(widgets.kind));
        widgets.swaggerUrl = buildUrlWidget(accentFor(widgets.kind));
        widgets.resourceToken = buildTokenWidget(accentFor(widgets.kind));
        widgets.resourceTokenExpiry = valueLabel("pending");
        addRow(form, 0, "Prerequisites", widgets.prereq);
        addRow(form, 1, "Tunnel", widgets.tunnel);
        addRow(form, 2, "Runtime", widgets.app);
        addRow(form, 3, "MCP", widgets.mcpUrl.panel);
        addRow(form, 4, "API", widgets.apiUrl.panel);
        addRow(form, 5, "Swagger", widgets.swaggerUrl.panel);
        addRow(form, 6, "Token API", widgets.resourceToken.panel);
        addRow(form, 7, "Caduca", widgets.resourceTokenExpiry);
        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildModuleControls(ResourceWidgets widgets, boolean includeResetCampus) {
        JPanel card = new JPanel(new BorderLayout());
        card.setOpaque(false);
        JPanel buttons = buttonRow();
        widgets.start = new JButton("Arrancar modulo");
        widgets.stop = new JButton("Parar modulo");
        stylePrimaryButton(widgets.start, accentFor(widgets.kind));
        styleDangerButton(widgets.stop);
        switch (widgets.kind) {
            case OUTLOOK -> {
                widgets.start.addActionListener(e -> runAsync(this::startOutlook));
                widgets.stop.addActionListener(e -> runAsync(this::stopOutlook));
            }
            case QBID -> {
                widgets.start.addActionListener(e -> runAsync(this::startQbid));
                widgets.stop.addActionListener(e -> runAsync(this::stopQbid));
            }
            case CAMPUS -> {
                widgets.start.addActionListener(e -> runAsync(this::startCampus));
                widgets.stop.addActionListener(e -> runAsync(this::stopCampus));
            }
            case TRELLO -> {
                widgets.start.addActionListener(e -> runAsync(this::startTrello));
                widgets.stop.addActionListener(e -> runAsync(this::stopTrello));
            }
        }
        buttons.add(widgets.start);
        buttons.add(widgets.stop);
        if (includeResetCampus) {
            widgets.relaunchLogin = new JButton("Relanzar login");
            styleSecondaryButton(widgets.relaunchLogin, accentFor(widgets.kind));
            widgets.relaunchLogin.setEnabled(false);
            widgets.relaunchLogin.addActionListener(e -> resetCampusLogin());
            buttons.add(widgets.relaunchLogin);
        }
        card.add(buttons, BorderLayout.NORTH);
        return card;
    }

    private JPanel buildActivityPanel() {
        JPanel card = card("Actividad", "Consola de shell y runtimes", ACCENT_DARK);
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setFont(MONO_FONT);
        logArea.setForeground(LOG_FG);
        logArea.setBackground(LOG_BG);
        logArea.setCaretColor(LOG_FG);
        logArea.setBorder(new EmptyBorder(12, 12, 12, 12));

        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(LOG_BG);
        scroll.setPreferredSize(new Dimension(0, 260));

        JPanel actions = buttonRow();
        JButton copyLogs = new JButton("Copiar log");
        JButton clearLogs = new JButton("Limpiar");
        styleSecondaryButton(copyLogs, ACCENT_DARK);
        styleNeutralButton(clearLogs);
        copyLogs.addActionListener(e -> copyLogs());
        clearLogs.addActionListener(e -> clearLogs());
        actions.add(copyLogs);
        actions.add(clearLogs);

        card.add(scroll, BorderLayout.CENTER);
        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    private void login() throws Exception {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (username.isBlank() || password.isBlank()) throw new IllegalStateException("Debes introducir usuario y password.");
        ClientLoginResponse response = remoteLauncherService.login(resolveControlPlaneUrl(), new ClientLoginRequest(
                username, password, machineIdField.getText().trim(), clientVersionField.getText().trim()
        ));
        LauncherSessionResources launcherSessionResources = launcherResourceTokenService.issueSessionResources(
                response,
                normalizeResources(response),
                machineIdField.getText().trim(),
                clientVersionField.getText().trim()
        );
        List<ClientMcpResourceResponse> resources = launcherSessionResources.resources();
        controlPlaneSession = new ControlPlaneSession(resolveControlPlaneUrl(), response.accessToken(), response.externalUserId(),
                machineIdField.getText().trim(), clientVersionField.getText().trim(), response.username(), response.email(),
                response.bootstrap(), resources, launcherSessionResources.tokenContext().issuerUri(),
                launcherSessionResources.tokenContext().sharedSecret());
        SwingUtilities.invokeLater(() -> {
            set(loginStatus, "Sesion preparada", OK);
            loginUser.setText(response.username());
            loginResources.setText(resources.stream().map(ClientMcpResourceResponse::displayName).reduce((a, b) -> a + ", " + b).orElse("none"));
            loginButton.setEnabled(false);
            logoutButton.setEnabled(true);
            setLoginFieldsEditable(false);
            updateSessionBadge("Sesion preparada", OK);
            updateAccessRequirement(outlookWidgets);
            updateAccessRequirement(qbidWidgets);
            updateAccessRequirement(campusWidgets);
            updateAccessRequirement(trelloWidgets);
        });
        appendLog("[launcher] Sesion iniciada contra tunnel." + System.lineSeparator());
        applyResource(outlookWidgets, controlPlaneSession.resourceFor("outlook"));
        applyResource(qbidWidgets, controlPlaneSession.resourceFor("qbid"));
        applyResource(campusWidgets, controlPlaneSession.resourceFor("campus"));
        applyResource(trelloWidgets, controlPlaneSession.resourceFor("trello"));
        restorePersistedTrelloConnection();
    }

    private void triggerLogin() {
        if (loginButton != null && loginButton.isEnabled()) {
            runAsync(this::login);
        }
    }

    private void triggerQbidStart() {
        if (qbidWidgets != null && qbidWidgets.start != null && qbidWidgets.start.isEnabled()) {
            runAsync(this::startQbid);
        }
    }

    private void handleTabSelection(int tabIndex) {
        if (frame != null && frame.getRootPane() != null) {
            JButton defaultButton = switch (tabIndex) {
                case 0 -> loginButton;
                case 2 -> qbidWidgets == null ? null : qbidWidgets.start;
                default -> null;
            };
            frame.getRootPane().setDefaultButton(defaultButton);
        }

        switch (tabIndex) {
            case 1 -> refreshPrerequisitesAsync(ManagedMcpKind.OUTLOOK, outlookWidgets);
            case 2 -> refreshPrerequisitesAsync(ManagedMcpKind.QBID, qbidWidgets);
            case 3 -> refreshPrerequisitesAsync(ManagedMcpKind.CAMPUS, campusWidgets);
            case 4 -> refreshPrerequisitesAsync(ManagedMcpKind.TRELLO, trelloWidgets);
            default -> {
            }
        }
    }

    private void validateOutlook() throws Exception { validateResource(ManagedMcpKind.OUTLOOK, outlookWidgets); }
    private void validateQbid() throws Exception {
        validateResource(ManagedMcpKind.QBID, qbidWidgets);
        if (qbidUserField.getText().trim().isBlank() || new String(qbidPasswordField.getPassword()).isBlank()) {
            throw new IllegalStateException("Debes indicar usuario y password de qBid.");
        }
    }
    private void validateCampus() throws Exception { validateResource(ManagedMcpKind.CAMPUS, campusWidgets); }
    private void validateTrello() throws Exception {
        validateResource(ManagedMcpKind.TRELLO, trelloWidgets);
        if (trelloConnection == null || trelloConnection.accessToken() == null || trelloConnection.accessToken().isBlank()) {
            throw new IllegalStateException("Debes conectar Trello antes de arrancar el modulo.");
        }
        if (trelloApiKey == null || trelloApiKey.isBlank()) {
            throw new IllegalStateException("No hay API key de Trello configurada.");
        }
    }

    private void validateResource(ManagedMcpKind kind, ResourceWidgets widgets) throws Exception {
        if (controlPlaneSession == null) {
            throw new IllegalStateException("Primero debes iniciar sesion.");
        }
        List<String> errors = remoteLauncherService.validatePrerequisites(controlPlaneSession, kind);
        if (!errors.isEmpty()) {
            set(widgets.prereq, "Prerequisites: blocked", WARN);
            throw new IllegalStateException(String.join(System.lineSeparator(), errors));
        }
        set(widgets.prereq, "Prerequisites: ready", OK);
    }

    private void refreshPrerequisitesAsync(ManagedMcpKind kind, ResourceWidgets widgets) {
        if (widgets == null || widgets.prereq == null) {
            return;
        }
        if (controlPlaneSession == null) {
            set(widgets.prereq, "Prerequisites: inicia sesion", MUTED);
            return;
        }
        CompletableFuture.runAsync(() -> {
            try {
                List<String> errors = remoteLauncherService.validatePrerequisites(controlPlaneSession, kind);
                SwingUtilities.invokeLater(() -> {
                    if (errors.isEmpty()) {
                        set(widgets.prereq, "Prerequisites: ready", OK);
                    } else {
                        set(widgets.prereq, "Prerequisites: blocked", WARN);
                    }
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> set(widgets.prereq, "Prerequisites: error", ERR));
                appendLog("[launcher] ERROR: " + ex.getMessage() + System.lineSeparator());
            }
        }, executor);
    }

    private void startOutlook() throws Exception {
        BootstrapResponse bootstrap = requireBootstrap(ManagedMcpKind.OUTLOOK);
        if (bootstrap.authExposureMode() != AuthExposureMode.CENTRAL_AUTH) throw new IllegalStateException("Se requiere CENTRAL_AUTH.");
        validateOutlook();
        remoteLauncherService.startManagedTunnel(ManagedMcpKind.OUTLOOK, bootstrap);
        set(outlookWidgets.tunnel, "Tunnel: activo", OK);
        RuntimeSettings settings = remoteLauncherService.runtimeSettingsFromBootstrap(bootstrap);
        remoteLauncherService.writeGeneratedEnv(settings);
        set(outlookWidgets.app, "Runtime: arrancando", WARN);
        outlookRuntimeService.start(bootstrap, controlPlaneSession);
        set(outlookWidgets.app, "Runtime: activo", OK);
        setUrl(outlookWidgets.swaggerUrl, outlookRuntimeService.getSwaggerUrl(), true);
        setPublicUrlActions(outlookWidgets, true);
        SwingUtilities.invokeLater(() -> { outlookWidgets.start.setEnabled(false); outlookWidgets.stop.setEnabled(true); });
    }

    private void stopOutlook() {
        outlookRuntimeService.stop();
        remoteLauncherService.stopTunnel(ManagedMcpKind.OUTLOOK);
        set(outlookWidgets.tunnel, "Tunnel: detenido", MUTED);
        set(outlookWidgets.app, "Runtime: detenido", MUTED);
        setUrl(outlookWidgets.swaggerUrl, null, false);
        setPublicUrlActions(outlookWidgets, false);
        SwingUtilities.invokeLater(() -> { outlookWidgets.start.setEnabled(controlPlaneSession != null); outlookWidgets.stop.setEnabled(false); });
    }

    private void validateQbidCredentials() throws Exception {
        String user = qbidUserField.getText().trim();
        String pass = new String(qbidPasswordField.getPassword());
        if (user.isBlank() || pass.isBlank()) throw new IllegalStateException("Debes indicar usuario y password de qBid.");
        qbidRuntimeService.validateCredentials(user, pass);
        set(qbidWidgets.prereq, "Prerequisites: ready", OK);
    }

    private void startQbid() throws Exception {
        BootstrapResponse bootstrap = requireBootstrap(ManagedMcpKind.QBID);
        if (bootstrap.authExposureMode() != AuthExposureMode.CENTRAL_AUTH) throw new IllegalStateException("Se requiere CENTRAL_AUTH.");
        validateQbid();
        validateQbidCredentials();
        remoteLauncherService.startManagedTunnel(ManagedMcpKind.QBID, bootstrap);
        set(qbidWidgets.tunnel, "Tunnel: activo", OK);
        qbidRuntimeService.start(bootstrap, controlPlaneSession, qbidUserField.getText().trim(), new String(qbidPasswordField.getPassword()));
        set(qbidWidgets.app, "Runtime: activo", OK);
        setUrl(qbidWidgets.swaggerUrl, qbidRuntimeService.getSwaggerUrl(), true);
        setPublicUrlActions(qbidWidgets, true);
        SwingUtilities.invokeLater(() -> {
            qbidWidgets.start.setEnabled(false);
            qbidWidgets.stop.setEnabled(true);
            setQbidFieldsEditable(false);
        });
    }

    private void stopQbid() {
        qbidRuntimeService.stop();
        remoteLauncherService.stopTunnel(ManagedMcpKind.QBID);
        set(qbidWidgets.tunnel, "Tunnel: detenido", MUTED);
        set(qbidWidgets.app, "Runtime: detenido", MUTED);
        setUrl(qbidWidgets.swaggerUrl, null, false);
        setPublicUrlActions(qbidWidgets, false);
        SwingUtilities.invokeLater(() -> {
            qbidWidgets.start.setEnabled(controlPlaneSession != null);
            qbidWidgets.stop.setEnabled(false);
            setQbidFieldsEditable(true);
        });
    }

    private void startCampus() throws Exception {
        BootstrapResponse bootstrap = requireBootstrap(ManagedMcpKind.CAMPUS);
        if (bootstrap.authExposureMode() != AuthExposureMode.CENTRAL_AUTH) {
            throw new IllegalStateException("Se requiere CENTRAL_AUTH.");
        }
        validateCampus();
        remoteLauncherService.startManagedTunnel(ManagedMcpKind.CAMPUS, bootstrap);
        set(campusWidgets.tunnel, "Tunnel: activo", OK);
        CampusEmbeddedPanel panel = campusRuntimeService.start(bootstrap, controlPlaneSession);
        installCampusPanelListener(panel);
        boolean authenticated = panel.isAuthenticated();
        set(campusWidgets.app, authenticated ? "Runtime: activo" : "Runtime: login requerido", authenticated ? OK : WARN);
        setUrl(campusWidgets.swaggerUrl, campusRuntimeService.getSwaggerUrl(), true);
        setPublicUrlActions(campusWidgets, true);
        SwingUtilities.invokeLater(() -> {
            campusWidgets.start.setEnabled(false);
            campusWidgets.stop.setEnabled(true);
            if (campusWidgets.relaunchLogin != null) {
                campusWidgets.relaunchLogin.setEnabled(true);
            }
        });
        if (!authenticated) {
            showCampusLoginDialog(panel, "Se requiere autenticacion para activar Campus.");
        }
    }

    private void stopCampus() {
        closeCampusLoginDialog(true);
        campusRuntimeService.stop();
        remoteLauncherService.stopTunnel(ManagedMcpKind.CAMPUS);
        set(campusWidgets.tunnel, "Tunnel: detenido", MUTED);
        set(campusWidgets.app, "Runtime: detenido", MUTED);
        setUrl(campusWidgets.swaggerUrl, null, false);
        setPublicUrlActions(campusWidgets, false);
        SwingUtilities.invokeLater(() -> {
            campusWidgets.start.setEnabled(controlPlaneSession != null);
            campusWidgets.stop.setEnabled(false);
            if (campusWidgets.relaunchLogin != null) {
                campusWidgets.relaunchLogin.setEnabled(false);
            }
        });
    }

    private void startTrello() throws Exception {
        BootstrapResponse bootstrap = requireBootstrap(ManagedMcpKind.TRELLO);
        if (bootstrap.authExposureMode() != AuthExposureMode.CENTRAL_AUTH) {
            throw new IllegalStateException("Se requiere CENTRAL_AUTH.");
        }
        validateTrello();
        remoteLauncherService.startManagedTunnel(ManagedMcpKind.TRELLO, bootstrap);
        set(trelloWidgets.tunnel, "Tunnel: activo", OK);
        set(trelloWidgets.app, "Runtime: arrancando", WARN);
        trelloRuntimeService.start(bootstrap, controlPlaneSession, trelloApiKey, trelloConnection);
        set(trelloWidgets.app, "Runtime: activo", OK);
        setUrl(trelloWidgets.swaggerUrl, trelloRuntimeService.getSwaggerUrl(), true);
        setPublicUrlActions(trelloWidgets, true);
        SwingUtilities.invokeLater(this::updateTrelloConnectionUi);
    }

    private void stopTrello() {
        trelloRuntimeService.stop();
        remoteLauncherService.stopTunnel(ManagedMcpKind.TRELLO);
        set(trelloWidgets.tunnel, "Tunnel: detenido", MUTED);
        set(trelloWidgets.app, "Runtime: detenido", MUTED);
        setUrl(trelloWidgets.swaggerUrl, null, false);
        setPublicUrlActions(trelloWidgets, false);
        SwingUtilities.invokeLater(this::updateTrelloConnectionUi);
    }

    private void connectTrello() throws Exception {
        if (controlPlaneSession == null) {
            throw new IllegalStateException("Primero debes iniciar sesion.");
        }
        requireBootstrap(ManagedMcpKind.TRELLO);
        TrelloConnection previous = trelloConnection;
        SwingUtilities.invokeLater(() -> setTrelloConnectionState("Trello: esperando autorizacion", WARN, previous));
        try {
            TrelloConnection connection = trelloAuthorizationService.authorize(trelloApiKey);
            trelloConnection = connection;
            persistTrelloConnection(connection);
            appendLog("[launcher] Trello conectado como " + connection.displayName() + "." + System.lineSeparator());
        } catch (Exception exception) {
            SwingUtilities.invokeLater(this::updateTrelloConnectionUi);
            throw exception;
        }
        SwingUtilities.invokeLater(this::updateTrelloConnectionUi);
    }

    private void reconnectTrello() throws Exception {
        if (trelloRuntimeService.isRunning()) {
            stopTrello();
        }
        connectTrello();
    }

    private void disconnectTrello() {
        TrelloConnection previousConnection = trelloConnection;
        if (trelloRuntimeService.isRunning()) {
            stopTrello();
        }
        trelloConnection = null;
        clearPersistedTrelloConnection();
        Exception revokeFailure = null;
        if (previousConnection != null
                && previousConnection.accessToken() != null
                && !previousConnection.accessToken().isBlank()
                && trelloApiKey != null
                && !trelloApiKey.isBlank()) {
            try {
                trelloAuthorizationService.revoke(trelloApiKey, previousConnection.accessToken());
            } catch (Exception exception) {
                revokeFailure = exception;
            }
        }
        appendLog("[launcher] Conexion Trello eliminada del equipo local." + System.lineSeparator());
        SwingUtilities.invokeLater(this::updateTrelloConnectionUi);
        if (revokeFailure != null) {
            throw new IllegalStateException(
                    "La conexion local se ha eliminado, pero no he podido revocar el token en Trello: "
                            + revokeFailure.getMessage(),
                    revokeFailure
            );
        }
    }

    private void applyResource(ResourceWidgets widgets, ClientMcpResourceResponse resource) {
        if (widgets == null) return;
        BootstrapResponse bootstrap = resource == null ? null : resource.bootstrap();
        if (bootstrap == null) { resetResource(widgets); return; }
        SwingUtilities.invokeLater(() -> {
            if (widgets.tunnelId != null) widgets.tunnelId.setText(value(bootstrap.tunnelId()));
            if (widgets.host != null) widgets.host.setText(value(bootstrap.mcpHostname()));
            if (widgets.issuer != null) widgets.issuer.setText(value(bootstrap.issuerUri()));
            if (widgets.jwk != null) widgets.jwk.setText(value(bootstrap.jwkSetUri()));
            if (widgets.audience != null) widgets.audience.setText(value(bootstrap.requiredAudience()));
            if (widgets.scope != null) widgets.scope.setText(value(bootstrap.requiredScope()));
            if (widgets.resourceName != null) widgets.resourceName.setText(value(bootstrap.resourceName()));
            setUrl(widgets.apiUrl, apiUrl(bootstrap), false);
            setUrl(widgets.mcpUrl, mcpUrl(bootstrap), false);
            setUrl(widgets.swaggerUrl, null, false);
            setToken(widgets.resourceToken, resource == null || resource.resourceToken() == null ? null : resource.resourceToken().accessToken());
            if (widgets.resourceTokenExpiry != null) {
                widgets.resourceTokenExpiry.setText(value(formatResourceTokenExpiry(resource == null ? null : resource.resourceToken())));
            }
            setPublicUrlActions(widgets, false);
            if (widgets.kind == ManagedMcpKind.TRELLO) {
                updateTrelloConnectionUi();
            } else if (widgets.start != null) {
                widgets.start.setEnabled(true);
            }
        });
    }

    private BootstrapResponse requireBootstrap(ManagedMcpKind kind) {
        if (controlPlaneSession == null) throw new IllegalStateException("Primero debes iniciar sesion.");
        BootstrapResponse bootstrap = controlPlaneSession.bootstrapFor(kind.resourceKey());
        if (bootstrap == null) throw new IllegalStateException("No hay bootstrap para " + kind.displayName());
        return bootstrap;
    }

    private void logout() {
        stopOutlook();
        stopQbid();
        stopCampus();
        stopTrello();
        controlPlaneSession = null;
        trelloConnection = null;
        resetLogin();
        resetResource(outlookWidgets);
        resetResource(qbidWidgets);
        resetResource(campusWidgets);
        resetResource(trelloWidgets);
    }

    private void restorePersistedTrelloConnection() {
        if (controlPlaneSession == null || controlPlaneSession.bootstrapFor(ManagedMcpKind.TRELLO.resourceKey()) == null) {
            return;
        }

        String storedToken;
        try {
            storedToken = trelloLocalTokenStore.loadAccessToken().orElse(null);
        } catch (Exception exception) {
            appendLog("[launcher] No he podido leer la conexion Trello guardada: "
                    + exception.getMessage() + System.lineSeparator());
            SwingUtilities.invokeLater(this::updateTrelloConnectionUi);
            return;
        }

        if (storedToken == null || storedToken.isBlank()) {
            SwingUtilities.invokeLater(this::updateTrelloConnectionUi);
            return;
        }

        SwingUtilities.invokeLater(() -> setTrelloConnectionState("Trello: restaurando conexion", WARN, null));
        try {
            TrelloConnection restoredConnection = trelloAuthorizationService.resolveConnection(trelloApiKey, storedToken);
            trelloConnection = restoredConnection;
            persistTrelloConnection(restoredConnection);
            appendLog("[launcher] Conexion Trello restaurada para "
                    + restoredConnection.displayName() + "." + System.lineSeparator());
        } catch (Exception exception) {
            trelloConnection = null;
            clearPersistedTrelloConnection();
            appendLog("[launcher] La conexion Trello guardada ya no es valida y se ha eliminado."
                    + System.lineSeparator());
        }
        SwingUtilities.invokeLater(this::updateTrelloConnectionUi);
    }

    private void persistTrelloConnection(TrelloConnection connection) {
        try {
            trelloLocalTokenStore.saveAccessToken(connection.accessToken());
        } catch (Exception exception) {
            appendLog("[launcher] AVISO: Trello conectado, pero no he podido persistir el token localmente: "
                    + exception.getMessage() + System.lineSeparator());
        }
    }

    private void clearPersistedTrelloConnection() {
        try {
            trelloLocalTokenStore.clear();
        } catch (Exception exception) {
            appendLog("[launcher] AVISO: No he podido limpiar el token Trello local: "
                    + exception.getMessage() + System.lineSeparator());
        }
    }

    private void resetLogin() {
        if (loginStatus != null) {
            set(loginStatus, "Sesion: no autenticada", MUTED);
            loginUser.setText("pending");
            loginResources.setText("pending");
            loginButton.setEnabled(true);
            logoutButton.setEnabled(false);
            setLoginFieldsEditable(true);
            setQbidFieldsEditable(true);
            updateSessionBadge("Sesion no autenticada", MUTED);
        }
        updateAccessRequirement(outlookWidgets);
        updateAccessRequirement(qbidWidgets);
        updateAccessRequirement(campusWidgets);
        updateAccessRequirement(trelloWidgets);
    }

    private void resetResource(ResourceWidgets widgets) {
        if (widgets == null) return;
        if (widgets.tunnelId != null) widgets.tunnelId.setText("pending");
        if (widgets.host != null) widgets.host.setText("pending");
        if (widgets.issuer != null) widgets.issuer.setText("");
        if (widgets.jwk != null) widgets.jwk.setText("");
        if (widgets.audience != null) widgets.audience.setText("");
        if (widgets.scope != null) widgets.scope.setText("");
        if (widgets.resourceName != null) widgets.resourceName.setText("");
        set(widgets.prereq, controlPlaneSession == null ? "Prerequisites: inicia sesion" : "Prerequisites: pending", MUTED);
        set(widgets.tunnel, "Tunnel: detenido", MUTED);
        set(widgets.app, "Runtime: detenido", MUTED);
        setUrl(widgets.apiUrl, null, false);
        setUrl(widgets.mcpUrl, null, false);
        setUrl(widgets.swaggerUrl, null, false);
        setToken(widgets.resourceToken, null);
        if (widgets.resourceTokenExpiry != null) widgets.resourceTokenExpiry.setText("pending");
        setPublicUrlActions(widgets, false);
        if (widgets.connectedAccount != null) widgets.connectedAccount.setText("");
        if (widgets.connectionState != null) set(widgets.connectionState, "Trello: no conectado", MUTED);
        if (widgets.start != null) widgets.start.setEnabled(false);
        if (widgets.stop != null) widgets.stop.setEnabled(false);
        if (widgets.relaunchLogin != null) widgets.relaunchLogin.setEnabled(false);
        updateAccessRequirement(widgets);
        if (widgets.kind == ManagedMcpKind.TRELLO) {
            updateTrelloConnectionUi();
        }
    }

    private void loadDefaults() {
        machineIdField.setText(System.getenv().getOrDefault("CONTROL_PLANE_MACHINE_ID", System.getenv().getOrDefault("COMPUTERNAME", "")));
        clientVersionField.setText(DEFAULT_CLIENT_VERSION);
    }

    private String resolveControlPlaneUrl() {
        return developmentMode ? controlPlaneUrlField.getText().trim() : DEFAULT_CONTROL_PLANE_URL;
    }

    private List<ClientMcpResourceResponse> normalizeResources(ClientLoginResponse response) {
        if (response.resources() != null && !response.resources().isEmpty()) {
            return response.resources();
        }

        List<ClientMcpResourceResponse> resources = new ArrayList<>();
        if (response.bootstrap() != null) {
            resources.add(new ClientMcpResourceResponse(
                    ManagedMcpKind.OUTLOOK.resourceKey(),
                    ManagedMcpKind.OUTLOOK.displayName(),
                    response.bootstrap(),
                    null
            ));
        }
        return resources;
    }

    private String formatResourceTokenExpiry(ResourceAccessTokenResponse token) {
        if (token == null || token.expiresAt() == null) {
            return null;
        }
        return token.expiresAt().toString();
    }

    private String extractUrl(String text) {
        int idx = text == null ? -1 : text.indexOf("http");
        return idx >= 0 ? text.substring(idx).trim() : null;
    }

    private void copyUrlValue(String label, String url) {
        String resolved = extractUrl(url);
        if (resolved == null) {
            return;
        }
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(resolved), null);
        appendLog("[launcher] " + label + " copiada: " + resolved + System.lineSeparator());
    }

    private void copyTokenValue(String label, String value) {
        String resolved = value == null ? null : value.trim();
        if (resolved == null || resolved.isBlank() || "pending".equalsIgnoreCase(resolved)) {
            return;
        }
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(resolved), null);
        appendLog("[launcher] " + label + " copiado al portapapeles." + System.lineSeparator());
    }

    private void openUrl(String url) {
        if (url == null || !Desktop.isDesktopSupported()) return;
        try { Desktop.getDesktop().browse(URI.create(url)); } catch (Exception ignored) { }
    }

    private void copyLogs() {
        if (logArea == null || logArea.getText().isBlank()) {
            return;
        }
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(logArea.getText()), null);
        appendLog("[launcher] Log copiado al portapapeles." + System.lineSeparator());
    }

    private void clearLogs() {
        if (logArea == null) {
            return;
        }
        logArea.setText("");
    }

    private void appendLog(String message) {
        if (logArea == null) return;
        SwingUtilities.invokeLater(() -> {
            logArea.append(message);
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void runAsync(ThrowingRunnable runnable) {
        CompletableFuture.runAsync(() -> {
            try {
                runnable.run();
            } catch (Exception ex) {
                appendLog("[launcher] ERROR: " + ex.getMessage() + System.lineSeparator());
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE));
            }
        }, executor);
    }

    private void shutdown() {
        stopOutlook();
        stopQbid();
        stopCampus();
        stopTrello();
        trelloAuthorizationService.shutdown();
        GuiLogPublisher.unregister(logConsumer);
        remoteLauncherService.shutdown();
        executor.shutdownNow();
    }

    private void resetCampusLogin() {
        CampusEmbeddedPanel panel = campusRuntimeService.getEmbeddedPanel();
        if (panel == null) {
            appendLog("[launcher] ERROR: Campus MCP no esta activo." + System.lineSeparator());
            return;
        }
        set(campusWidgets.app, "Runtime: login requerido", WARN);
        panel.resetSession();
        appendLog("[launcher] Login de Campus reiniciado." + System.lineSeparator());
    }

    private JPanel contentPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(4, 4, 4, 4));
        return panel;
    }

    private JPanel card(String title, String eyebrow, Color accent) {
        SurfacePanel panel = new SurfacePanel(SURFACE, 28, blend(accent, BORDER, 0.28));
        panel.setLayout(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(12, 14, 12, 14));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(LABEL_FONT.deriveFont(15f));
        titleLabel.setForeground(INK);
        header.add(titleLabel);

        panel.add(header, BorderLayout.NORTH);
        return panel;
    }

    private JPanel formPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        return panel;
    }

    private JPanel buttonRow() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panel.setOpaque(false);
        return panel;
    }

    private void addRow(JPanel panel, int row, String label, java.awt.Component component) {
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.insets = new Insets(7, 0, 7, 14);
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.anchor = GridBagConstraints.NORTHWEST;

        JLabel rowLabel = new JLabel(label);
        rowLabel.setFont(LABEL_FONT);
        rowLabel.setForeground(MUTED);
        panel.add(rowLabel, labelConstraints);

        GridBagConstraints componentConstraints = new GridBagConstraints();
        componentConstraints.insets = new Insets(7, 0, 7, 0);
        componentConstraints.gridx = 1;
        componentConstraints.gridy = row;
        componentConstraints.fill = GridBagConstraints.HORIZONTAL;
        componentConstraints.weightx = 1.0;
        panel.add(component, componentConstraints);
    }

    private JTextField readonly() {
        JTextField field = new JTextField(26);
        field.setEditable(false);
        styleInput(field, false);
        return field;
    }

    private UrlWidget buildUrlWidget(Color accent) {
        JTextField field = new JTextField(28);
        field.setEditable(false);
        styleUrlField(field);
        JButton open = new JButton("Abrir");
        JButton copy = new JButton("Copiar");
        styleInlineButton(open);
        styleInlineButton(copy);
        open.setForeground(accentForButton(accent));
        copy.setForeground(accentForButton(accent));
        open.addActionListener(event -> openUrl(extractUrl(field.getText())));
        copy.addActionListener(event -> copyUrlValue("URL", field.getText()));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        actions.setOpaque(false);
        actions.add(open);
        actions.add(copy);

        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setOpaque(false);
        panel.add(field, BorderLayout.CENTER);
        panel.add(actions, BorderLayout.EAST);
        return new UrlWidget(panel, field, open, copy, actions);
    }

    private TokenWidget buildTokenWidget(Color accent) {
        JTextField field = new JTextField(28);
        field.setEditable(false);
        styleUrlField(field);
        JButton copy = new JButton("Copiar");
        styleInlineButton(copy);
        copy.setForeground(accentForButton(accent));
        copy.addActionListener(event -> copyTokenValue("Token", field.getText()));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        actions.setOpaque(false);
        actions.add(copy);

        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setOpaque(false);
        panel.add(field, BorderLayout.CENTER);
        panel.add(actions, BorderLayout.EAST);
        return new TokenWidget(panel, field, copy, actions);
    }

    private JLabel valueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setOpaque(true);
        label.setFont(BODY_FONT);
        label.setForeground(INK);
        label.setBackground(SURFACE_WASH);
        label.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(7, 10, 7, 10)
        ));
        return label;
    }

    private void set(JLabel label, String text, Color color) {
        if (label == null) return;
        label.setText(text);
        label.setForeground(color == MUTED ? MUTED : color.darker());
        label.setBackground(blend(color, SURFACE, 0.14));
    }

    private String value(String text) {
        return text == null || text.isBlank() ? "pending" : text;
    }

    private JLabel chipLabel(String text, Color background, Color foreground) {
        JLabel label = new JLabel(text);
        label.setOpaque(true);
        label.setFont(CHIP_FONT);
        label.setForeground(foreground);
        label.setBackground(background);
        label.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(new Color(foreground.getRed(), foreground.getGreen(), foreground.getBlue(), 96), 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        return label;
    }

    private JLabel buildParagraph(String text, int width) {
        return buildParagraph(text, width, MUTED);
    }

    private JLabel buildParagraph(String text, int width, Color color) {
        JLabel label = new JLabel("<html><div style='width:" + width + "px;'>" + text + "</div></html>");
        label.setFont(BODY_FONT);
        label.setForeground(color);
        return label;
    }

    private JLabel accessRequirementLabel(int width) {
        JLabel label = buildParagraph("Se requiere que el usuario este autenticado para usar este modulo.", width, blend(ERR, BLACK_SOFT, 0.72));
        label.setVisible(controlPlaneSession == null);
        return label;
    }

    private void styleInput(JTextComponent component, boolean editable) {
        component.setFont(BODY_FONT);
        component.setForeground(editable ? INK : MUTED);
        component.setBackground(editable ? SURFACE : SURFACE_SOFT);
        component.setCaretColor(INK);
        component.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(editable ? BORDER : blend(MUTED, BORDER, 0.45), 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
    }

    private void styleUrlField(JTextField field) {
        field.setFont(MONO_FONT);
        field.setForeground(INK);
        field.setBackground(SURFACE_SOFT);
        field.setCaretColor(INK);
        field.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(blend(BLUE_MAIN, BORDER, 0.35), 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
    }

    private void stylePrimaryButton(JButton button, Color accent) {
        styleButton(button, BLUE_MAIN, PINK, WHITE, new Color(CYAN.getRed(), CYAN.getGreen(), CYAN.getBlue(), 130), true);
    }

    private void styleSecondaryButton(JButton button, Color accent) {
        styleButton(button, WHITE, WHITE, accentForButton(accent), new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 120), false);
    }

    private void styleNeutralButton(JButton button) {
        styleButton(button, SURFACE, SURFACE, INK, new Color(BORDER.getRed(), BORDER.getGreen(), BORDER.getBlue(), 190), false);
    }

    private void styleDangerButton(JButton button) {
        styleButton(button, BLACK_SOFT, ACCENT_DARK, WHITE, new Color(PINK.getRed(), PINK.getGreen(), PINK.getBlue(), 110), false);
    }

    private void styleButton(JButton button, Color start, Color end, Color foreground, Color border, boolean glow) {
        button.setFont(LABEL_FONT);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setRolloverEnabled(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(8, 14, 8, 14));
        button.setUI(new StyledButtonUI(start, end, foreground, border, glow));
    }

    private void styleInlineButton(JButton button) {
        button.setFont(CHIP_FONT);
        button.setForeground(ACCENT_DARK);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setRolloverEnabled(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(6, 10, 6, 10));
        button.setUI(new StyledButtonUI(WHITE, WHITE, ACCENT_DARK, new Color(BLUE_MAIN.getRed(), BLUE_MAIN.getGreen(), BLUE_MAIN.getBlue(), 110), false));
    }

    private void updateSessionBadge(String text, Color color) {
        if (sessionBadge == null) {
            return;
        }
        sessionBadge.setText(text);
        sessionBadge.setForeground(color == MUTED ? new Color(221, 228, 241) : WHITE);
        sessionBadge.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 58));
        sessionBadge.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(new Color(color.getRed(), color.getGreen(), color.getBlue(), 120), 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
    }

    private void updateAccessRequirement(ResourceWidgets widgets) {
        if (widgets == null || widgets.accessRequirement == null) {
            return;
        }
        boolean authenticated = controlPlaneSession != null;
        widgets.accessRequirement.setVisible(!authenticated);
        widgets.accessRequirement.setText(authenticated ? "" : "<html><div style='width:300px;'>Se requiere que el usuario este autenticado para usar este modulo.</div></html>");
        widgets.accessRequirement.revalidate();
        widgets.accessRequirement.repaint();
    }

    private void updateTrelloConnectionUi() {
        if (trelloWidgets == null) {
            return;
        }
        boolean sessionReady = controlPlaneSession != null && controlPlaneSession.bootstrapFor(ManagedMcpKind.TRELLO.resourceKey()) != null;
        boolean connected = trelloConnection != null && trelloConnection.accessToken() != null && !trelloConnection.accessToken().isBlank();
        boolean running = trelloRuntimeService.isRunning();

        if (!sessionReady) {
            setTrelloConnectionState("Trello: inicia sesion", MUTED, null);
        } else if (connected) {
            setTrelloConnectionState("Trello: conectado", OK, trelloConnection);
        } else {
            setTrelloConnectionState("Trello: no conectado", MUTED, null);
        }

        if (trelloWidgets.connect != null) {
            trelloWidgets.connect.setEnabled(sessionReady && !connected && !running);
        }
        if (trelloWidgets.reconnect != null) {
            trelloWidgets.reconnect.setEnabled(sessionReady && connected && !running);
        }
        if (trelloWidgets.disconnect != null) {
            trelloWidgets.disconnect.setEnabled(sessionReady && connected && !running);
        }
        if (trelloWidgets.start != null) {
            trelloWidgets.start.setEnabled(sessionReady && connected && !running);
        }
        if (trelloWidgets.stop != null) {
            trelloWidgets.stop.setEnabled(running);
        }
    }

    private void setTrelloConnectionState(String status, Color color, TrelloConnection connection) {
        if (trelloWidgets == null) {
            return;
        }
        if (trelloWidgets.connectionState != null) {
            set(trelloWidgets.connectionState, status, color);
        }
        if (trelloWidgets.connectedAccount != null) {
            trelloWidgets.connectedAccount.setText(connection == null ? "pending" : connection.displayName());
            trelloWidgets.connectedAccount.setCaretPosition(0);
        }
    }

    private Color accentFor(ManagedMcpKind kind) {
        return switch (kind) {
            case OUTLOOK -> OUTLOOK_ACCENT;
            case QBID -> QBID_ACCENT;
            case CAMPUS -> CAMPUS_ACCENT;
            case TRELLO -> TRELLO_ACCENT;
        };
    }

    private String moduleTitle(ManagedMcpKind kind) {
        return switch (kind) {
            case OUTLOOK -> "Opera Outlook bot con acceso directo a la API, al endpoint MCP y a Swagger";
            case QBID -> "Publica QBid bot y arranca el servicio con las credenciales locales del operador";
            case CAMPUS -> "Gestiona Campus bot y recicla la sesion desde el navegador embebido";
            case TRELLO -> "Gestiona Trello bot y usa el token local del operador capturado desde navegador";
        };
    }

    private String moduleDescription(ManagedMcpKind kind) {
        return switch (kind) {
            case OUTLOOK -> "Este panel concentra bootstrap, prerequisitos y ciclo de vida del servicio de Outlook bot para comprobar la publicacion remota sin salir de la shell.";
            case QBID -> "QBid bot necesita credenciales locales para arrancar. El estado expone las rutas importantes y evita validaciones manuales redundantes.";
            case CAMPUS -> "Campus bot combina estado operativo con una cabina JCEF embebida para reutilizar la sesion del login y reducir friccion durante las pruebas.";
            case TRELLO -> "Trello bot captura la autorizacion del usuario desde el navegador y publica tools locales para tableros, listas y tarjetas.";
        };
    }

    private String moduleName(ManagedMcpKind kind) {
        return switch (kind) {
            case OUTLOOK -> "Outlook bot";
            case QBID -> "QBid bot";
            case CAMPUS -> "Campus bot";
            case TRELLO -> "Trello bot";
        };
    }

    private String apiUrl(BootstrapResponse bootstrap) {
        return bootstrap == null ? null : "http://localhost:" + bootstrap.localPort();
    }

    private String mcpUrl(BootstrapResponse bootstrap) {
        if (bootstrap == null || bootstrap.mcpPublicBaseUrl() == null || bootstrap.mcpPublicBaseUrl().isBlank()) {
            return null;
        }
        return trimTrailingSlash(bootstrap.mcpPublicBaseUrl()) + "/mcp";
    }

    private String trimTrailingSlash(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private void setUrl(UrlWidget widget, String url, boolean openEnabled) {
        if (widget == null) {
            return;
        }
        String value = url == null || url.isBlank() ? "pending" : url;
        widget.field.setText(value);
        boolean hasUrl = url != null && !url.isBlank();
        widget.copy.setEnabled(hasUrl);
        widget.open.setEnabled(hasUrl && openEnabled);
    }

    private void setToken(TokenWidget widget, String token) {
        if (widget == null) {
            return;
        }
        String value = token == null || token.isBlank() ? "pending" : token;
        widget.field.setText(value);
        widget.field.setCaretPosition(0);
        boolean hasToken = token != null && !token.isBlank();
        widget.copy.setEnabled(hasToken);
    }

    private void setPublicUrlActions(ResourceWidgets widgets, boolean openEnabled) {
        if (widgets == null) {
            return;
        }
        boolean hasApi = widgets.apiUrl != null && extractUrl(widgets.apiUrl.field.getText()) != null;
        boolean hasMcp = widgets.mcpUrl != null && extractUrl(widgets.mcpUrl.field.getText()) != null;
        boolean hasSwagger = widgets.swaggerUrl != null && extractUrl(widgets.swaggerUrl.field.getText()) != null;
        if (widgets.apiUrl != null) {
            applyUrlActionMode(widgets.apiUrl, true, false, hasApi, false);
        }
        if (widgets.mcpUrl != null) {
            applyUrlActionMode(widgets.mcpUrl, true, false, hasMcp, false);
        }
        if (widgets.swaggerUrl != null) {
            applyUrlActionMode(widgets.swaggerUrl, false, true, hasSwagger, openEnabled);
        }
    }

    private void applyUrlActionMode(UrlWidget widget,
                                    boolean allowCopy,
                                    boolean allowOpen,
                                    boolean hasValue,
                                    boolean openEnabled) {
        widget.copy.setVisible(allowCopy);
        widget.open.setVisible(allowOpen);
        widget.copy.setEnabled(allowCopy && hasValue);
        widget.open.setEnabled(allowOpen && hasValue && openEnabled);
        widget.actions.revalidate();
        widget.actions.repaint();
    }

    private void setLoginFieldsEditable(boolean editable) {
        if (usernameField != null) {
            usernameField.setEditable(editable);
            styleInput(usernameField, editable);
        }
        if (passwordField != null) {
            passwordField.setEditable(editable);
            styleInput(passwordField, editable);
        }
        if (controlPlaneUrlField != null) {
            controlPlaneUrlField.setEditable(editable && developmentMode);
            styleInput(controlPlaneUrlField, editable && developmentMode);
        }
    }

    private void setQbidFieldsEditable(boolean editable) {
        if (qbidUserField != null) {
            qbidUserField.setEditable(editable);
            styleInput(qbidUserField, editable);
        }
        if (qbidPasswordField != null) {
            qbidPasswordField.setEditable(editable);
            styleInput(qbidPasswordField, editable);
        }
    }

    private Color accentForButton(Color accent) {
        return blend(accent, INK, 0.70);
    }

    private static Color blend(Color source, Color target, double ratio) {
        double clamped = Math.max(0.0, Math.min(1.0, ratio));
        int red = (int) Math.round(source.getRed() * clamped + target.getRed() * (1.0 - clamped));
        int green = (int) Math.round(source.getGreen() * clamped + target.getGreen() * (1.0 - clamped));
        int blue = (int) Math.round(source.getBlue() * clamped + target.getBlue() * (1.0 - clamped));
        return new Color(red, green, blue);
    }

    private List<Image> loadApplicationIcons() {
        List<Image> icons = new ArrayList<>();
        try {
            var resource = getClass().getResource("/assets/ceac-ai-app-icon.png");
            if (resource != null) {
                icons.add(ImageIO.read(resource));
            }
        } catch (Exception ignored) {
        }
        try {
            var resource = getClass().getResource("/assets/ceac-ai-mini-icon.png");
            if (resource != null) {
                icons.add(ImageIO.read(resource));
            }
        } catch (Exception ignored) {
        }
        return icons;
    }

    private Image loadTabsLogoImage() {
        try {
            var resource = getClass().getResource("/assets/ceac-ai-logo.png");
            return resource == null ? null : ImageIO.read(resource);
        } catch (Exception ignored) {
            return null;
        }
    }

    private void installCampusPanelListener(CampusEmbeddedPanel panel) {
        panel.setSessionListener(new CampusEmbeddedPanel.SessionListener() {
            @Override
            public void onLoginRequired(String message) {
                if (campusWidgets != null) {
                    set(campusWidgets.app, "Runtime: login requerido", WARN);
                }
                showCampusLoginDialog(panel, message);
            }

            @Override
            public void onAuthenticationSuccess(String message) {
                if (campusWidgets != null) {
                    set(campusWidgets.app, "Runtime: activo", OK);
                }
                closeCampusLoginDialog(false);
            }
        });
    }

    private void showCampusLoginDialog(CampusEmbeddedPanel panel, String message) {
        if (panel == null) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            if (frame == null) {
                return;
            }
            JDialog dialog = ensureCampusLoginDialog();
            if (dialog.getContentPane() != panel.asComponent()) {
                if (panel.getParent() != null) {
                    panel.getParent().remove(panel);
                }
                dialog.setContentPane(panel.asComponent());
                dialog.revalidate();
            }
            dialog.setTitle("Login Campus");
            dialog.setMinimumSize(new Dimension(960, 700));
            dialog.setSize(1120, 760);
            dialog.setLocationRelativeTo(frame);
            if (!dialog.isVisible() && message != null && !message.isBlank()) {
                appendLog("[launcher] " + message + System.lineSeparator());
            }
            campusDialogInternalClose = false;
            if (!dialog.isVisible()) {
                dialog.setVisible(true);
            } else {
                dialog.toFront();
                dialog.requestFocus();
            }
        });
    }

    private JDialog ensureCampusLoginDialog() {
        if (campusLoginDialog != null) {
            return campusLoginDialog;
        }
        JDialog dialog = new JDialog(frame, "Login Campus", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent event) {
                handleCampusDialogCloseRequest();
            }
        });
        campusLoginDialog = dialog;
        return dialog;
    }

    private void handleCampusDialogCloseRequest() {
        if (campusDialogInternalClose) {
            return;
        }
        CampusEmbeddedPanel panel = campusRuntimeService.getEmbeddedPanel();
        boolean authenticated = panel != null && panel.isAuthenticated();
        closeCampusLoginDialog(false);
        if (!authenticated) {
            appendLog("[launcher] Login de Campus cancelado. El modulo se detendra." + System.lineSeparator());
            runAsync(this::stopCampus);
        }
    }

    private void closeCampusLoginDialog(boolean dispose) {
        SwingUtilities.invokeLater(() -> {
            if (campusLoginDialog == null) {
                return;
            }
            campusDialogInternalClose = true;
            campusLoginDialog.setVisible(false);
            if (dispose) {
                campusLoginDialog.dispose();
                campusLoginDialog = null;
            }
            campusDialogInternalClose = false;
        });
    }

    @FunctionalInterface
    private interface ThrowingRunnable { void run() throws Exception; }

    private static final class ResourceWidgets {
        private final ManagedMcpKind kind;
        private JLabel tunnelId;
        private JLabel host;
        private JTextField issuer;
        private JTextField jwk;
        private JTextField audience;
        private JTextField scope;
        private JTextField resourceName;
        private JLabel prereq;
        private JLabel tunnel;
        private JLabel app;
        private UrlWidget apiUrl;
        private UrlWidget mcpUrl;
        private UrlWidget swaggerUrl;
        private TokenWidget resourceToken;
        private JLabel resourceTokenExpiry;
        private JButton start;
        private JButton stop;
        private JButton relaunchLogin;
        private JLabel accessRequirement;
        private JLabel connectionState;
        private JTextField connectedAccount;
        private JButton connect;
        private JButton reconnect;
        private JButton disconnect;

        private ResourceWidgets(ManagedMcpKind kind) { this.kind = kind; }
    }

    private static final class UrlWidget {
        private final JPanel panel;
        private final JTextField field;
        private final JButton open;
        private final JButton copy;
        private final JPanel actions;

        private UrlWidget(JPanel panel, JTextField field, JButton open, JButton copy, JPanel actions) {
            this.panel = panel;
            this.field = field;
            this.open = open;
            this.copy = copy;
            this.actions = actions;
        }
    }

    private static final class TokenWidget {
        private final JPanel panel;
        private final JTextField field;
        private final JButton copy;
        private final JPanel actions;

        private TokenWidget(JPanel panel, JTextField field, JButton copy, JPanel actions) {
            this.panel = panel;
            this.field = field;
            this.copy = copy;
            this.actions = actions;
        }
    }

    private static final class GradientPanel extends JPanel {
        private GradientPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, APP_BG, getWidth(), getHeight(), APP_BG_DEEP));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(new Color(CYAN.getRed(), CYAN.getGreen(), CYAN.getBlue(), 54));
            g2.fillOval(-80, -60, 360, 260);
            g2.setColor(new Color(PINK.getRed(), PINK.getGreen(), PINK.getBlue(), 44));
            g2.fillOval(getWidth() - 340, -20, 320, 240);
            g2.setColor(new Color(BLUE_MAIN.getRed(), BLUE_MAIN.getGreen(), BLUE_MAIN.getBlue(), 42));
            g2.fillOval(getWidth() / 2 - 160, getHeight() - 180, 320, 220);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static final class SurfacePanel extends JPanel {
        private final Color fill;
        private final Color stroke;
        private final int arc;

        private SurfacePanel(Color fill, int arc, Color stroke) {
            this.fill = fill;
            this.arc = arc;
            this.stroke = stroke;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            if (stroke != null) {
                g2.setColor(stroke);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static final class DashboardTabsUI extends BasicTabbedPaneUI {
        private final Image logo;

        private DashboardTabsUI(Image logo) {
            this.logo = logo;
        }

        @Override
        protected void installDefaults() {
            super.installDefaults();
            tabInsets = new Insets(12, 18, 12, 18);
            selectedTabPadInsets = new Insets(0, 0, 0, 0);
            tabAreaInsets = new Insets(0, 0, 12, 94);
            contentBorderInsets = new Insets(0, 0, 0, 0);
        }

        @Override
        protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (isSelected) {
                g2.setPaint(new GradientPaint(x, y, BLUE_MAIN, x + w, y + h, PINK));
            } else {
                g2.setColor(new Color(255, 255, 255, 18));
            }
            g2.fillRoundRect(x + 2, y + 2, w - 4, h - 4, 18, 18);
            if (isSelected) {
                g2.setColor(new Color(CYAN.getRed(), CYAN.getGreen(), CYAN.getBlue(), 92));
                g2.drawRoundRect(x + 2, y + 2, w - 5, h - 5, 18, 18);
            }
            g2.dispose();
        }

        @Override
        protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
        }

        @Override
        protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
        }

        @Override
        protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
        }

        @Override
        protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setFont(LABEL_FONT.deriveFont(14f));
            g2.setColor(isSelected ? WHITE : new Color(222, 231, 245));
            BasicGraphicsUtils.drawStringUnderlineCharAt(
                    tabPane,
                    g2,
                    title,
                    tabPane.getDisplayedMnemonicIndexAt(tabIndex),
                    textRect.x,
                    textRect.y + metrics.getAscent()
            );
            g2.dispose();
        }

        @Override
        protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {
            super.paintTabArea(g, tabPlacement, selectedIndex);
            if (logo == null || tabPlacement != TOP) {
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int size = 46;
            int x = tabPane.getWidth() - size - 16;
            int y = 4;

            g2.drawImage(logo, x, y, size, size, null);
            g2.dispose();
        }
    }

    private static final class StyledButtonUI extends BasicButtonUI {
        private final Color start;
        private final Color end;
        private final Color border;
        private final boolean glow;

        private StyledButtonUI(Color start, Color end, Color foreground, Color border, boolean glow) {
            this.start = start;
            this.end = end;
            this.border = border;
            this.glow = glow;
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            AbstractButton button = (AbstractButton) c;
            ButtonModel model = button.getModel();
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = c.getWidth();
            int height = c.getHeight();
            int arc = 18;

            if (glow && button.isEnabled() && model.isRollover()) {
                g2.setColor(new Color(CYAN.getRed(), CYAN.getGreen(), CYAN.getBlue(), 54));
                g2.fillRoundRect(1, 1, width - 2, height - 2, arc + 6, arc + 6);
            }

            Paint fill = start.equals(end)
                    ? start
                    : new GradientPaint(0, 0, start, width, height, end);
            if (!button.isEnabled()) {
                fill = blend(start, GRAY_MEDIUM, 0.45);
            } else if (model.isPressed()) {
                fill = start.equals(end)
                        ? blend(start, BLACK_SOFT, 0.80)
                        : new GradientPaint(0, 0, blend(start, BLACK_SOFT, 0.82), width, height, blend(end, BLACK_SOFT, 0.82));
            } else if (model.isRollover()) {
                fill = start.equals(end)
                        ? blend(start, CYAN, 0.86)
                        : new GradientPaint(0, 0, blend(start, CYAN, 0.90), width, height, blend(end, CYAN, 0.90));
            }

            g2.setPaint(fill);
            g2.fillRoundRect(0, 0, width - 1, height - 1, arc, arc);
            g2.setColor(button.isEnabled() ? border : new Color(border.getRed(), border.getGreen(), border.getBlue(), 80));
            g2.drawRoundRect(0, 0, width - 1, height - 1, arc, arc);

            FontMetrics metrics = g2.getFontMetrics(button.getFont());
            int textX = (width - metrics.stringWidth(button.getText())) / 2;
            int textY = (height - metrics.getHeight()) / 2 + metrics.getAscent();
            g2.setFont(button.getFont());
            Color foreground = button.getForeground();
            g2.setColor(button.isEnabled() ? foreground : new Color(foreground.getRed(), foreground.getGreen(), foreground.getBlue(), 140));
            g2.drawString(button.getText(), textX, textY);
            g2.dispose();
        }
    }
}


