package tools.ceac.ai.desktop.ui;

import tools.ceac.ai.mcp.outlook.OutlookMcpRuntimeApplication;
import tools.ceac.ai.mcp.campus.interfaces.desktop.CampusEmbeddedPanel;
import tools.ceac.ai.desktop.launcher.AuthExposureMode;
import tools.ceac.ai.desktop.launcher.BootstrapResponse;
import tools.ceac.ai.desktop.launcher.CampusRuntimeService;
import tools.ceac.ai.desktop.launcher.ClientMcpResourceResponse;
import tools.ceac.ai.desktop.launcher.ClientLoginRequest;
import tools.ceac.ai.desktop.launcher.ClientLoginResponse;
import tools.ceac.ai.desktop.launcher.ControlPlaneSession;
import tools.ceac.ai.desktop.launcher.ManagedMcpKind;
import tools.ceac.ai.desktop.launcher.QbidRuntimeService;
import tools.ceac.ai.desktop.launcher.RemoteLauncherService;
import tools.ceac.ai.desktop.launcher.RuntimeSettings;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.net.URI;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Main Swing window of CEAC AI Tools Desktop.
 *
 * <p>The window is the product shell. It is responsible for:
 *
 * <ul>
 *   <li>desktop login against the control plane</li>
 *   <li>projecting bootstrap data into the UI</li>
 *   <li>starting and stopping each managed resource</li>
 *   <li>embedding resource-specific UI such as the Campus JCEF panel</li>
 * </ul>
 *
 * <p>It deliberately does not contain provisioning logic or direct Cloudflare management. Those
 * concerns stay in {@code RemoteLauncherService} and in the backend control plane.
 */
class CeacLauncherWindow {

    private static final String DEFAULT_CONTROL_PLANE_URL = "https://control.dartmaker.com";
    private static final String DEFAULT_CLIENT_VERSION = "1.0.0";
    private static final Color OK = new Color(31, 123, 77);
    private static final Color WARN = new Color(180, 103, 28);
    private static final Color ERR = new Color(165, 55, 55);
    private static final Color MUTED = new Color(90, 90, 90);

    private final RemoteLauncherService remoteLauncherService = new RemoteLauncherService();
    private final QbidRuntimeService qbidRuntimeService = new QbidRuntimeService();
    private final CampusRuntimeService campusRuntimeService = new CampusRuntimeService();
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final Consumer<String> logConsumer = this::appendLog;
    private final Consumer<AppRuntimeState> outlookStateConsumer = this::updateOutlookRuntimeState;
    private final boolean developmentMode = Boolean.parseBoolean(System.getenv().getOrDefault("DARTMAKER_DEV_MODE", "false"));

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
    private JButton loginButton;
    private JButton logoutButton;

    private ResourceWidgets outlookWidgets;
    private ResourceWidgets qbidWidgets;
    private ResourceWidgets campusWidgets;
    private JTextField qbidUserField;
    private JPasswordField qbidPasswordField;
    private JPanel campusPanelHost;

    private ConfigurableApplicationContext outlookContext;
    private ControlPlaneSession controlPlaneSession;

    public void show() {
        GuiLogPublisher.register(logConsumer);
        SwingUtilities.invokeLater(this::createWindow);
    }

    private void createWindow() {
        frame = new JFrame(developmentMode ? "CEAC AI Tools [DEV]" : "CEAC AI Tools");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(12, 12));
        frame.getContentPane().setBackground(new Color(245, 241, 233));
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { shutdown(); }
        });

        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(14, 14, 0, 14));
        header.setOpaque(false);
        JLabel title = new JLabel("CEAC AI Tools");
        title.setFont(title.getFont().deriveFont(java.awt.Font.BOLD, 28f));
        JLabel subtitle = new JLabel("Launcher centralizado para Outlook MCP, QBid MCP y Campus MCP.");
        subtitle.setForeground(MUTED);
        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setOpaque(false);
        titlePanel.add(title);
        titlePanel.add(subtitle);
        header.add(titlePanel, BorderLayout.CENTER);
        frame.add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Login", buildLoginTab());
        tabs.addTab("Outlook MCP", buildResourceTab(ManagedMcpKind.OUTLOOK));
        tabs.addTab("QBid MCP", buildResourceTab(ManagedMcpKind.QBID));
        tabs.addTab("Campus MCP", buildResourceTab(ManagedMcpKind.CAMPUS));
        frame.add(tabs, BorderLayout.CENTER);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setPreferredSize(new Dimension(1200, 180));
        logScroll.setBorder(BorderFactory.createTitledBorder("Actividad"));
        frame.add(logScroll, BorderLayout.SOUTH);

        loadDefaults();
        resetLogin();
        resetResource(outlookWidgets);
        resetResource(qbidWidgets);
        frame.setSize(1200, 900);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        LauncherEvents.register(outlookStateConsumer);
    }

    private JPanel buildLoginTab() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panel.add(buildLoginAccessCard());
        panel.add(buildLoginSummaryCard());
        return panel;
    }

    private JPanel buildLoginAccessCard() {
        JPanel card = card("Panel de control");
        JPanel form = formPanel();
        controlPlaneUrlField = new JTextField(DEFAULT_CONTROL_PLANE_URL, 28);
        controlPlaneUrlField.setEditable(developmentMode);
        usernameField = new JTextField(28);
        passwordField = new JPasswordField(28);
        machineIdField = new JTextField(28);
        machineIdField.setEditable(false);
        clientVersionField = new JTextField(DEFAULT_CLIENT_VERSION, 28);
        clientVersionField.setEditable(false);
        int row = 0;
        if (developmentMode) {
            addRow(form, row++, "Control plane URL", controlPlaneUrlField);
        }
        addRow(form, row++, "Usuario o email", usernameField);
        addRow(form, row++, "Password", passwordField);
        addRow(form, row++, "Machine ID", machineIdField);
        addRow(form, row, "Client version", clientVersionField);
        JPanel buttons = new JPanel();
        loginButton = new JButton("Iniciar sesion en panel de control");
        logoutButton = new JButton("Cerrar sesion");
        loginButton.addActionListener(e -> runAsync(this::login));
        logoutButton.addActionListener(e -> logout());
        buttons.add(loginButton);
        buttons.add(logoutButton);
        card.add(form, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildLoginSummaryCard() {
        JPanel card = card("Sesion");
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
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JPanel top = new JPanel(new GridLayout(1, 2, 12, 12));
        ResourceWidgets widgets = new ResourceWidgets(kind);
        if (kind == ManagedMcpKind.OUTLOOK) {
            outlookWidgets = widgets;
            top.add(buildResourceBootstrapCard(widgets));
            top.add(buildResourceRuntimeCard(widgets, false));
            panel.add(top, BorderLayout.CENTER);
            panel.add(buildOutlookActions(widgets), BorderLayout.SOUTH);
        } else {
            qbidWidgets = widgets;
            top.add(buildQbidAccessCard(widgets));
            top.add(buildResourceBootstrapCard(widgets));
            panel.add(top, BorderLayout.CENTER);
            panel.add(buildQbidActions(widgets), BorderLayout.SOUTH);
        }
        return panel;
    }

    private JPanel buildCampusTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        campusWidgets = new ResourceWidgets(ManagedMcpKind.CAMPUS);
        JPanel top = new JPanel(new GridLayout(1, 2, 12, 12));
        top.add(buildResourceBootstrapCard(campusWidgets));
        top.add(buildResourceRuntimeCard(campusWidgets, false));

        campusPanelHost = new JPanel(new BorderLayout());
        campusPanelHost.setBorder(BorderFactory.createTitledBorder("Login Campus"));
        campusPanelHost.setBackground(Color.WHITE);
        campusPanelHost.add(new JLabel("Arranca Campus MCP para cargar aqui el navegador embebido y reciclar la sesion."), BorderLayout.NORTH);

        panel.add(top, BorderLayout.NORTH);
        panel.add(campusPanelHost, BorderLayout.CENTER);
        panel.add(buildCampusActions(campusWidgets), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildQbidAccessCard(ResourceWidgets widgets) {
        JPanel card = card("Credenciales qBid");
        JPanel form = formPanel();
        qbidUserField = new JTextField(28);
        qbidPasswordField = new JPasswordField(28);
        widgets.prereq = valueLabel("Prerequisites: pending");
        widgets.tunnel = valueLabel("Tunnel: detenido");
        widgets.app = valueLabel("Runtime: detenido");
        widgets.swagger = valueLabel("Swagger: pending");
        addRow(form, 0, "Usuario qBid", qbidUserField);
        addRow(form, 1, "Password qBid", qbidPasswordField);
        addRow(form, 2, "Prerequisites", widgets.prereq);
        addRow(form, 3, "Tunnel", widgets.tunnel);
        addRow(form, 4, "Runtime", widgets.app);
        addRow(form, 5, "Swagger", widgets.swagger);
        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildResourceBootstrapCard(ResourceWidgets widgets) {
        JPanel card = card("Bootstrap " + widgets.kind.displayName());
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

    private JPanel buildResourceRuntimeCard(ResourceWidgets widgets, boolean qbid) {
        JPanel card = card("Estado " + widgets.kind.displayName());
        JPanel form = formPanel();
        widgets.prereq = valueLabel("Prerequisites: pending");
        widgets.tunnel = valueLabel("Tunnel: detenido");
        widgets.app = valueLabel("Runtime: detenido");
        widgets.swagger = valueLabel("Swagger: pending");
        addRow(form, 0, "Prerequisites", widgets.prereq);
        addRow(form, 1, "Tunnel", widgets.tunnel);
        addRow(form, 2, "Runtime", widgets.app);
        addRow(form, 3, "Swagger", widgets.swagger);
        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildOutlookActions(ResourceWidgets widgets) {
        JPanel card = card("Operativa Outlook");
        JPanel buttons = new JPanel();
        widgets.start = new JButton("Arrancar MCP");
        widgets.stop = new JButton("Parar MCP");
        widgets.copy = new JButton("Copiar MCP URL");
        JButton validate = new JButton("Validar");
        JButton openSwagger = new JButton("Abrir Swagger");
        JButton copySwagger = new JButton("Copiar Swagger");
        validate.addActionListener(e -> runAsync(this::validateOutlook));
        widgets.start.addActionListener(e -> runAsync(this::startOutlook));
        widgets.stop.addActionListener(e -> runAsync(this::stopOutlook));
        widgets.copy.addActionListener(e -> copyUrl(ManagedMcpKind.OUTLOOK));
        openSwagger.addActionListener(e -> openUrl(extractUrl(widgets.swagger.getText())));
        copySwagger.addActionListener(e -> copyLabelUrl(widgets.swagger));
        buttons.add(validate); buttons.add(widgets.start); buttons.add(widgets.stop); buttons.add(widgets.copy); buttons.add(openSwagger); buttons.add(copySwagger);
        card.add(buttons, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildQbidActions(ResourceWidgets widgets) {
        JPanel card = card("Operativa qBid");
        JPanel buttons = new JPanel();
        widgets.start = new JButton("Arrancar MCP");
        widgets.stop = new JButton("Parar MCP");
        widgets.copy = new JButton("Copiar MCP URL");
        JButton validateCreds = new JButton("Validar credenciales");
        JButton validate = new JButton("Validar estado");
        JButton openSwagger = new JButton("Abrir Swagger");
        JButton copySwagger = new JButton("Copiar Swagger");
        validateCreds.addActionListener(e -> runAsync(this::validateQbidCredentials));
        validate.addActionListener(e -> runAsync(this::validateQbid));
        widgets.start.addActionListener(e -> runAsync(this::startQbid));
        widgets.stop.addActionListener(e -> runAsync(this::stopQbid));
        widgets.copy.addActionListener(e -> copyUrl(ManagedMcpKind.QBID));
        openSwagger.addActionListener(e -> openUrl(extractUrl(widgets.swagger.getText())));
        copySwagger.addActionListener(e -> copyLabelUrl(widgets.swagger));
        buttons.add(validateCreds); buttons.add(validate); buttons.add(widgets.start); buttons.add(widgets.stop); buttons.add(widgets.copy); buttons.add(openSwagger); buttons.add(copySwagger);
        card.add(buttons, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildCampusActions(ResourceWidgets widgets) {
        JPanel card = card("Operativa Campus");
        JPanel buttons = new JPanel();
        widgets.start = new JButton("Arrancar MCP");
        widgets.stop = new JButton("Parar MCP");
        widgets.copy = new JButton("Copiar MCP URL");
        JButton validate = new JButton("Validar estado");
        JButton openSwagger = new JButton("Abrir Swagger");
        JButton copySwagger = new JButton("Copiar Swagger");
        JButton resetLogin = new JButton("Reiniciar login Campus");
        validate.addActionListener(e -> runAsync(this::validateCampus));
        widgets.start.addActionListener(e -> runAsync(this::startCampus));
        widgets.stop.addActionListener(e -> runAsync(this::stopCampus));
        widgets.copy.addActionListener(e -> copyUrl(ManagedMcpKind.CAMPUS));
        openSwagger.addActionListener(e -> openUrl(extractUrl(widgets.swagger.getText())));
        copySwagger.addActionListener(e -> copyLabelUrl(widgets.swagger));
        resetLogin.addActionListener(e -> resetCampusLogin());
        buttons.add(validate); buttons.add(widgets.start); buttons.add(widgets.stop); buttons.add(widgets.copy); buttons.add(openSwagger); buttons.add(copySwagger); buttons.add(resetLogin);
        card.add(buttons, BorderLayout.CENTER);
        return card;
    }

    private void login() throws Exception {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (username.isBlank() || password.isBlank()) throw new IllegalStateException("Debes introducir usuario y password.");
        ClientLoginResponse response = remoteLauncherService.login(resolveControlPlaneUrl(), new ClientLoginRequest(
                username, password, machineIdField.getText().trim(), clientVersionField.getText().trim()
        ));
        List<ClientMcpResourceResponse> resources = normalizeResources(response);
        controlPlaneSession = new ControlPlaneSession(resolveControlPlaneUrl(), response.accessToken(), response.externalUserId(),
                machineIdField.getText().trim(), clientVersionField.getText().trim(), response.username(), response.email(),
                response.bootstrap(), resources);
        SwingUtilities.invokeLater(() -> {
            set(loginStatus, "Sesion preparada", OK);
            loginUser.setText(response.username());
            loginResources.setText(resources.stream().map(ClientMcpResourceResponse::displayName).reduce((a, b) -> a + ", " + b).orElse("none"));
            loginButton.setEnabled(false);
            logoutButton.setEnabled(true);
        });
        applyBootstrap(outlookWidgets, controlPlaneSession.bootstrapFor("outlook"));
        applyBootstrap(qbidWidgets, controlPlaneSession.bootstrapFor("qbid"));
        applyBootstrap(campusWidgets, controlPlaneSession.bootstrapFor("campus"));
    }

    private void validateOutlook() throws Exception { validateResource(ManagedMcpKind.OUTLOOK, outlookWidgets); }
    private void validateQbid() throws Exception {
        validateResource(ManagedMcpKind.QBID, qbidWidgets);
        if (qbidUserField.getText().trim().isBlank() || new String(qbidPasswordField.getPassword()).isBlank()) {
            throw new IllegalStateException("Debes indicar usuario y password de qBid.");
        }
    }
    private void validateCampus() throws Exception { validateResource(ManagedMcpKind.CAMPUS, campusWidgets); }

    private void validateResource(ManagedMcpKind kind, ResourceWidgets widgets) throws Exception {
        List<String> errors = remoteLauncherService.validatePrerequisites(controlPlaneSession, kind);
        if (!errors.isEmpty()) {
            set(widgets.prereq, "Prerequisites: blocked", WARN);
            throw new IllegalStateException(String.join(System.lineSeparator(), errors));
        }
        set(widgets.prereq, "Prerequisites: ready", OK);
    }

    private void startOutlook() throws Exception {
        BootstrapResponse bootstrap = requireBootstrap(ManagedMcpKind.OUTLOOK);
        if (bootstrap.authExposureMode() != AuthExposureMode.CENTRAL_AUTH) throw new IllegalStateException("Se requiere CENTRAL_AUTH.");
        validateOutlook();
        remoteLauncherService.startManagedTunnel(ManagedMcpKind.OUTLOOK, bootstrap);
        set(outlookWidgets.tunnel, "Tunnel: activo", OK);
        RuntimeSettings settings = remoteLauncherService.runtimeSettingsFromBootstrap(bootstrap);
        remoteLauncherService.writeGeneratedEnv(settings);
        LauncherEvents.publish(new AppRuntimeState("Starting", null));
        outlookContext = CompletableFuture.supplyAsync(() -> new SpringApplicationBuilder(OutlookMcpRuntimeApplication.class)
                .headless(false).properties(settings.toSpringProperties()).run(), executor).join();
        SwingUtilities.invokeLater(() -> { outlookWidgets.start.setEnabled(false); outlookWidgets.stop.setEnabled(true); });
    }

    private void stopOutlook() {
        ConfigurableApplicationContext context = outlookContext;
        outlookContext = null;
        if (context != null) context.close();
        remoteLauncherService.stopTunnel(ManagedMcpKind.OUTLOOK);
        set(outlookWidgets.tunnel, "Tunnel: detenido", MUTED);
        set(outlookWidgets.app, "Runtime: detenido", MUTED);
        set(outlookWidgets.swagger, "Swagger: pending", MUTED);
        SwingUtilities.invokeLater(() -> { outlookWidgets.start.setEnabled(controlPlaneSession != null); outlookWidgets.stop.setEnabled(false); });
    }

    private void validateQbidCredentials() throws Exception {
        String user = qbidUserField.getText().trim();
        String pass = new String(qbidPasswordField.getPassword());
        if (user.isBlank() || pass.isBlank()) throw new IllegalStateException("Debes indicar usuario y password de qBid.");
        qbidRuntimeService.validateCredentials(user, pass);
        set(qbidWidgets.prereq, "Credenciales qBid validadas", OK);
    }

    private void startQbid() throws Exception {
        BootstrapResponse bootstrap = requireBootstrap(ManagedMcpKind.QBID);
        if (bootstrap.authExposureMode() != AuthExposureMode.CENTRAL_AUTH) throw new IllegalStateException("Se requiere CENTRAL_AUTH.");
        validateQbid();
        validateQbidCredentials();
        remoteLauncherService.startManagedTunnel(ManagedMcpKind.QBID, bootstrap);
        set(qbidWidgets.tunnel, "Tunnel: activo", OK);
        qbidRuntimeService.start(bootstrap, qbidUserField.getText().trim(), new String(qbidPasswordField.getPassword()));
        set(qbidWidgets.app, "Runtime: activo", OK);
        set(qbidWidgets.swagger, "Swagger: " + qbidRuntimeService.getSwaggerUrl(), OK);
        SwingUtilities.invokeLater(() -> { qbidWidgets.start.setEnabled(false); qbidWidgets.stop.setEnabled(true); });
    }

    private void stopQbid() {
        qbidRuntimeService.stop();
        remoteLauncherService.stopTunnel(ManagedMcpKind.QBID);
        set(qbidWidgets.tunnel, "Tunnel: detenido", MUTED);
        set(qbidWidgets.app, "Runtime: detenido", MUTED);
        set(qbidWidgets.swagger, "Swagger: pending", MUTED);
        SwingUtilities.invokeLater(() -> { qbidWidgets.start.setEnabled(controlPlaneSession != null); qbidWidgets.stop.setEnabled(false); });
    }

    private void startCampus() throws Exception {
        BootstrapResponse bootstrap = requireBootstrap(ManagedMcpKind.CAMPUS);
        if (bootstrap.authExposureMode() != AuthExposureMode.CENTRAL_AUTH) {
            throw new IllegalStateException("Se requiere CENTRAL_AUTH.");
        }
        validateCampus();
        remoteLauncherService.startManagedTunnel(ManagedMcpKind.CAMPUS, bootstrap);
        set(campusWidgets.tunnel, "Tunnel: activo", OK);
        CampusEmbeddedPanel panel = campusRuntimeService.start(bootstrap);
        mountCampusPanel(panel);
        set(campusWidgets.app, "Runtime: activo", OK);
        set(campusWidgets.swagger, "Swagger: " + campusRuntimeService.getSwaggerUrl(), OK);
        SwingUtilities.invokeLater(() -> { campusWidgets.start.setEnabled(false); campusWidgets.stop.setEnabled(true); });
    }

    private void stopCampus() {
        campusRuntimeService.stop();
        remoteLauncherService.stopTunnel(ManagedMcpKind.CAMPUS);
        set(campusWidgets.tunnel, "Tunnel: detenido", MUTED);
        set(campusWidgets.app, "Runtime: detenido", MUTED);
        set(campusWidgets.swagger, "Swagger: pending", MUTED);
        clearCampusPanel();
        SwingUtilities.invokeLater(() -> { campusWidgets.start.setEnabled(controlPlaneSession != null); campusWidgets.stop.setEnabled(false); });
    }

    private void applyBootstrap(ResourceWidgets widgets, BootstrapResponse bootstrap) {
        if (widgets == null) return;
        if (bootstrap == null) { resetResource(widgets); return; }
        SwingUtilities.invokeLater(() -> {
            widgets.tunnelId.setText(value(bootstrap.tunnelId()));
            widgets.host.setText(value(bootstrap.mcpHostname()));
            widgets.issuer.setText(value(bootstrap.issuerUri()));
            widgets.jwk.setText(value(bootstrap.jwkSetUri()));
            widgets.audience.setText(value(bootstrap.requiredAudience()));
            widgets.scope.setText(value(bootstrap.requiredScope()));
            widgets.resourceName.setText(value(bootstrap.resourceName()));
            widgets.start.setEnabled(true);
            widgets.copy.setEnabled(true);
        });
    }

    private BootstrapResponse requireBootstrap(ManagedMcpKind kind) {
        if (controlPlaneSession == null) throw new IllegalStateException("Primero debes iniciar sesion.");
        BootstrapResponse bootstrap = controlPlaneSession.bootstrapFor(kind.resourceKey());
        if (bootstrap == null) throw new IllegalStateException("No hay bootstrap para " + kind.displayName());
        return bootstrap;
    }

    private void updateOutlookRuntimeState(AppRuntimeState state) {
        SwingUtilities.invokeLater(() -> {
            if (outlookWidgets == null) return;
            set(outlookWidgets.app, "Runtime: " + state.status(), color(state.status()));
            outlookWidgets.swagger.setText("Swagger: " + (state.swaggerUrl() == null ? "pending" : state.swaggerUrl()));
            if (!"Running".equalsIgnoreCase(state.status())) {
                outlookWidgets.start.setEnabled(controlPlaneSession != null);
                outlookWidgets.stop.setEnabled(false);
            }
        });
    }

    private void logout() {
        stopOutlook();
        stopQbid();
        stopCampus();
        controlPlaneSession = null;
        resetLogin();
        resetResource(outlookWidgets);
        resetResource(qbidWidgets);
        resetResource(campusWidgets);
    }

    private void resetLogin() {
        if (loginStatus != null) {
            set(loginStatus, "Sesion: no autenticada", MUTED);
            loginUser.setText("pending");
            loginResources.setText("pending");
            loginButton.setEnabled(true);
            logoutButton.setEnabled(false);
        }
    }

    private void resetResource(ResourceWidgets widgets) {
        if (widgets == null || widgets.tunnelId == null) return;
        widgets.tunnelId.setText("pending");
        widgets.host.setText("pending");
        widgets.issuer.setText("");
        widgets.jwk.setText("");
        widgets.audience.setText("");
        widgets.scope.setText("");
        widgets.resourceName.setText("");
        set(widgets.prereq, "Prerequisites: pending", MUTED);
        set(widgets.tunnel, "Tunnel: detenido", MUTED);
        set(widgets.app, "Runtime: detenido", MUTED);
        set(widgets.swagger, "Swagger: pending", MUTED);
        widgets.start.setEnabled(false);
        widgets.stop.setEnabled(false);
        widgets.copy.setEnabled(false);
        if (widgets.kind == ManagedMcpKind.CAMPUS) {
            clearCampusPanel();
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
                    response.bootstrap()
            ));
        }
        return resources;
    }

    private void copyUrl(ManagedMcpKind kind) {
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(requireBootstrap(kind).mcpPublicBaseUrl()), null);
            appendLog("[launcher] MCP URL copiada para " + kind.displayName() + System.lineSeparator());
        } catch (Exception ex) {
            appendLog("[launcher] ERROR: " + ex.getMessage() + System.lineSeparator());
        }
    }

    private void copyLabelUrl(JLabel label) {
        String url = extractUrl(label.getText());
        if (url == null) return;
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(url), null);
    }

    private String extractUrl(String text) {
        int idx = text == null ? -1 : text.indexOf("http");
        return idx >= 0 ? text.substring(idx).trim() : null;
    }

    private void openUrl(String url) {
        if (url == null || !Desktop.isDesktopSupported()) return;
        try { Desktop.getDesktop().browse(URI.create(url)); } catch (Exception ignored) { }
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
        LauncherEvents.unregister(outlookStateConsumer);
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
        panel.resetSession();
        appendLog("[launcher] Login de Campus reiniciado." + System.lineSeparator());
    }

    private void mountCampusPanel(CampusEmbeddedPanel panel) {
        SwingUtilities.invokeLater(() -> {
            campusPanelHost.removeAll();
            campusPanelHost.add(panel.asComponent(), BorderLayout.CENTER);
            campusPanelHost.revalidate();
            campusPanelHost.repaint();
        });
    }

    private void clearCampusPanel() {
        if (campusPanelHost == null) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            campusPanelHost.removeAll();
            JLabel placeholder = new JLabel("Arranca Campus MCP para cargar aqui el navegador embebido y reciclar la sesion.");
            placeholder.setForeground(MUTED);
            campusPanelHost.add(placeholder, BorderLayout.NORTH);
            campusPanelHost.revalidate();
            campusPanelHost.repaint();
        });
    }

    private JPanel card(String title) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.setBackground(Color.WHITE);
        return panel;
    }

    private JPanel formPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        return panel;
    }

    private void addRow(JPanel panel, int row, String label, java.awt.Component component) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(component, gbc);
    }

    private JTextField readonly() {
        JTextField field = new JTextField(26);
        field.setEditable(false);
        return field;
    }

    private JLabel valueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setOpaque(true);
        label.setBackground(new Color(240, 233, 219));
        label.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        return label;
    }

    private void set(JLabel label, String text, Color color) {
        if (label == null) return;
        label.setText(text);
        label.setForeground(color);
    }

    private String value(String text) {
        return text == null || text.isBlank() ? "pending" : text;
    }

    private Color color(String status) {
        if (status == null) return MUTED;
        String normalized = status.toLowerCase();
        if (normalized.contains("running") || normalized.contains("activo") || normalized.contains("ready")) return OK;
        if (normalized.contains("error") || normalized.contains("detenido")) return ERR;
        return WARN;
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
        private JLabel swagger;
        private JButton start;
        private JButton stop;
        private JButton copy;

        private ResourceWidgets(ManagedMcpKind kind) { this.kind = kind; }
    }
}
