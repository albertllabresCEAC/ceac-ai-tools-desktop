package com.alber.outlookdesktop.ui;

import com.alber.outlookdesktop.OutlookDesktopComMcpApplication;
import com.alber.outlookdesktop.launcher.AuthExposureMode;
import com.alber.outlookdesktop.launcher.BootstrapResponse;
import com.alber.outlookdesktop.launcher.ClientLoginRequest;
import com.alber.outlookdesktop.launcher.ClientLoginResponse;
import com.alber.outlookdesktop.launcher.ControlPlaneSession;
import com.alber.outlookdesktop.launcher.RemoteLauncherService;
import com.alber.outlookdesktop.launcher.RuntimeSettings;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * UI principal del launcher del cliente desktop.
 */
public class LauncherWindow {

    private static final String DEFAULT_CONTROL_PLANE_URL = "https://control.dartmaker.com";
    private static final String DEFAULT_CLIENT_VERSION = "1.0.0";

    private static final Color BACKGROUND = new Color(245, 241, 233);
    private static final Color SURFACE = new Color(255, 252, 247);
    private static final Color SURFACE_ALT = new Color(240, 233, 219);
    private static final Color BORDER = new Color(219, 208, 185);
    private static final Color BRAND_BLUE = new Color(32, 58, 208);
    private static final Color BRAND_BLUE_DARK = new Color(20, 32, 98);
    private static final Color BRAND_BLUE_SOFT = new Color(229, 235, 255);
    private static final Color GOLD = new Color(214, 183, 117);
    private static final Color INK = new Color(28, 37, 67);
    private static final Color MUTED = new Color(101, 110, 137);
    private static final Color SUCCESS = new Color(31, 123, 77);
    private static final Color WARNING = new Color(180, 103, 28);
    private static final Color DANGER = new Color(165, 55, 55);
    private static final Color LOG_BACKGROUND = new Color(14, 21, 49);
    private static final Color LOG_FOREGROUND = new Color(239, 241, 250);

    private static final Font TITLE_FONT = new Font("Serif", Font.BOLD, 30);
    private static final Font SECTION_FONT = new Font("SansSerif", Font.BOLD, 18);
    private static final Font LABEL_FONT = new Font("SansSerif", Font.BOLD, 13);
    private static final Font BODY_FONT = new Font("SansSerif", Font.PLAIN, 14);
    private static final Font MONO_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 13);

    private final RemoteLauncherService remoteLauncherService = new RemoteLauncherService();
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final Consumer<String> logConsumer = this::appendLog;
    private final Consumer<AppRuntimeState> appStateConsumer = this::updateAppRuntimeState;
    private final boolean developmentMode = Boolean.parseBoolean(
            System.getenv().getOrDefault("DARTMAKER_DEV_MODE", "false")
    );

    private ConfigurableApplicationContext applicationContext;
    private ControlPlaneSession controlPlaneSession;

    private JFrame frame;
    private JTextArea logArea;

    private JTextField controlPlaneUrlField;
    private JLabel controlPlaneUrlLabel;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField machineIdField;
    private JTextField clientVersionField;
    private JLabel loginStatusLabel;
    private JLabel loginUserLabel;
    private JLabel loginExternalUserIdLabel;
    private JLabel loginAuthModeLabel;
    private JLabel loginMcpUrlLabel;

    private JLabel tunnelIdLabel;
    private JLabel mcpHostLabel;
    private JTextField issuerField;
    private JTextField jwkField;
    private JTextField audienceField;
    private JTextField scopeField;
    private JTextField resourceNameField;
    private JLabel tunnelStatusLabel;
    private JLabel appStatusLabel;
    private JLabel swaggerLabel;
    private JLabel prerequisitesLabel;
    private JButton loginButton;
    private JButton logoutButton;
    private JButton startMcpButton;
    private JButton stopMcpButton;
    private JButton copyMcpUrlButton;

    public void show() {
        GuiLogPublisher.register(logConsumer);
        SwingUtilities.invokeLater(this::createWindow);
    }

    private void createWindow() {
        frame = new JFrame(developmentMode
                ? "CEAC | Outlook Desktop MCP [DEV]"
                : "CEAC | Outlook Desktop MCP");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ImageIcon appIcon = loadImageIcon("/assets/ceac-logo-square.png");
        if (appIcon != null) {
            frame.setIconImage(appIcon.getImage());
        }
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                shutdownLauncher();
            }
        });

        JPanel root = new JPanel(new BorderLayout(18, 18));
        root.setBackground(BACKGROUND);
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        root.add(buildHeroPanel(), BorderLayout.NORTH);
        root.add(buildTabsPanel(), BorderLayout.CENTER);
        root.add(buildLogsCard(), BorderLayout.SOUTH);

        frame.setContentPane(root);
        frame.setSize(new Dimension(1180, 900));
        frame.setMinimumSize(new Dimension(980, 780));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        loadInitialDefaults();
        updateLoginControls(false);
        updateMcpControls(false);
        LauncherEvents.register(appStateConsumer);
    }

    private JComponent buildHeroPanel() {
        GradientPanel hero = new GradientPanel(BRAND_BLUE_DARK, BRAND_BLUE);
        hero.setLayout(new BorderLayout(18, 18));
        hero.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));

        JPanel brandBlock = new JPanel();
        brandBlock.setOpaque(false);
        brandBlock.setLayout(new BoxLayout(brandBlock, BoxLayout.Y_AXIS));

        JLabel logoLabel = buildLogoLabel();
        logoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel titleLabel = new JLabel("Outlook Desktop MCP");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Control centralizado, runtime local y acceso OAuth listo para ChatGPT o Claude.");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 15));
        subtitleLabel.setForeground(new Color(232, 237, 255));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        brandBlock.add(logoLabel);
        brandBlock.add(Box.createVerticalStrut(10));
        brandBlock.add(titleLabel);
        brandBlock.add(Box.createVerticalStrut(6));
        brandBlock.add(subtitleLabel);

        JPanel rightColumn = new JPanel();
        rightColumn.setOpaque(false);
        rightColumn.setLayout(new BoxLayout(rightColumn, BoxLayout.Y_AXIS));
        JLabel badge = createPillLabel(developmentMode ? "Modo desarrollo" : "Modo central", GOLD, INK);
        badge.setAlignmentX(Component.RIGHT_ALIGNMENT);
        JLabel strap = new JLabel("<html><div style='text-align:right'>Login en panel de control, bootstrap remoto, tunnel gestionado y runtime Outlook local.</div></html>");
        strap.setFont(new Font("SansSerif", Font.BOLD, 14));
        strap.setForeground(Color.WHITE);
        strap.setAlignmentX(Component.RIGHT_ALIGNMENT);
        JLabel hint = new JLabel("<html><div style='text-align:right;color:#dfe6ff'>La primera pestana prepara contexto.<br/>La segunda valida, arranca y expone el MCP.</div></html>");
        hint.setFont(new Font("SansSerif", Font.PLAIN, 13));
        hint.setForeground(new Color(223, 230, 255));
        hint.setAlignmentX(Component.RIGHT_ALIGNMENT);

        rightColumn.add(badge);
        rightColumn.add(Box.createVerticalGlue());
        rightColumn.add(strap);
        rightColumn.add(Box.createVerticalStrut(8));
        rightColumn.add(hint);

        hero.add(brandBlock, BorderLayout.CENTER);
        hero.add(rightColumn, BorderLayout.EAST);
        return hero;
    }

    private JComponent buildTabsPanel() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.BOLD, 14));
        tabs.setBackground(SURFACE_ALT);
        tabs.setForeground(INK);
        tabs.addTab("Login", buildLoginTab());
        tabs.addTab("Outlook MCP", buildMcpTab());

        JPanel wrapper = createCardPanel();
        wrapper.setLayout(new BorderLayout());
        wrapper.add(tabs, BorderLayout.CENTER);
        return wrapper;
    }

    private JComponent buildLogsCard() {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout(0, 12));

        JPanel header = createCardHeader(
                "Actividad del launcher",
                "Eventos de login, bootstrap, cloudflared y runtime local."
        );

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setFont(MONO_FONT);
        logArea.setBackground(LOG_BACKGROUND);
        logArea.setForeground(LOG_FOREGROUND);
        logArea.setCaretColor(Color.WHITE);
        logArea.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(38, 52, 108), 1, true));
        scroll.getViewport().setBackground(LOG_BACKGROUND);
        scroll.setPreferredSize(new Dimension(940, 220));

        card.add(header, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JComponent buildLoginTab() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 18, 18));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        panel.add(buildLoginAccessCard());
        panel.add(buildLoginSessionCard());
        return panel;
    }

    private JComponent buildLoginAccessCard() {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout(0, 16));

        JPanel header = createCardHeader(
                "Panel de control",
                "Acceso al backend central. En flujo normal solo editas usuario y password."
        );

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = defaultGbc();

        controlPlaneUrlField = createReadOnlyField(System.getenv().getOrDefault("CONTROL_PLANE_URL", DEFAULT_CONTROL_PLANE_URL));
        if (developmentMode) {
            controlPlaneUrlField.setEditable(true);
            controlPlaneUrlField.setBackground(Color.WHITE);
        }
        controlPlaneUrlLabel = createValueLabel(resolveControlPlaneUrl());
        usernameField = createEditableField(System.getenv().getOrDefault("CONTROL_PLANE_LOGIN_USERNAME", ""));
        passwordField = createPasswordField(System.getenv().getOrDefault("CONTROL_PLANE_LOGIN_PASSWORD", ""));
        machineIdField = createReadOnlyField("");
        clientVersionField = createReadOnlyField(DEFAULT_CLIENT_VERSION);

        addRow(form, gbc, 0, "Control plane URL", developmentMode ? controlPlaneUrlField : controlPlaneUrlLabel);
        addRow(form, gbc, 1, "Usuario o email", usernameField);
        addRow(form, gbc, 2, "Password", passwordField);
        addRow(form, gbc, 3, "Machine ID", machineIdField);
        addRow(form, gbc, 4, "Client version", clientVersionField);

        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        loginButton = createPrimaryButton("Iniciar sesion en panel de control");
        loginButton.addActionListener(event -> runAsync(this::loginAndPrepareMcp));
        logoutButton = createSecondaryButton("Cerrar sesion");
        logoutButton.addActionListener(event -> logout());
        logoutButton.setEnabled(false);
        buttons.add(loginButton);
        buttons.add(logoutButton);

        card.add(header, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);
        return card;
    }

    private JComponent buildLoginSessionCard() {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout(0, 16));

        JPanel header = createCardHeader(
                "Contexto de sesion",
                "Estado que deja listo el panel de control antes de pasar al runtime Outlook."
        );

        JPanel summary = new JPanel(new GridBagLayout());
        summary.setOpaque(false);
        GridBagConstraints gbc = defaultGbc();

        loginStatusLabel = createValueLabel("Sesion: no autenticada");
        loginUserLabel = createValueLabel("pending");
        loginExternalUserIdLabel = createValueLabel("pending");
        loginAuthModeLabel = createValueLabel("pending");
        loginMcpUrlLabel = createValueLabel("pending");

        addRow(summary, gbc, 0, "Estado", loginStatusLabel);
        addRow(summary, gbc, 1, "Usuario autenticado", loginUserLabel);
        addRow(summary, gbc, 2, "External user ID", loginExternalUserIdLabel);
        addRow(summary, gbc, 3, "Modo auth", loginAuthModeLabel);
        addRow(summary, gbc, 4, "MCP URL", loginMcpUrlLabel);

        card.add(header, BorderLayout.NORTH);
        card.add(summary, BorderLayout.CENTER);
        card.add(createInfoStrip("La URL publica y el bootstrap aparecen aqui en cuanto el backend haya autenticado al usuario y resuelto el tunnel."), BorderLayout.SOUTH);
        return card;
    }

    private JComponent buildMcpTab() {
        JPanel panel = new JPanel(new BorderLayout(18, 18));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JPanel topRow = new JPanel(new GridLayout(1, 2, 18, 18));
        topRow.setOpaque(false);
        topRow.add(buildBootstrapCard());
        topRow.add(buildRuntimeCard());

        panel.add(topRow, BorderLayout.CENTER);
        panel.add(buildActionBarCard(), BorderLayout.SOUTH);
        return panel;
    }

    private JComponent buildBootstrapCard() {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout(0, 16));

        JPanel header = createCardHeader(
                "Bootstrap remoto",
                "Valores que entrega el control plane para exponer Outlook por Cloudflare con OAuth."
        );

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = defaultGbc();

        tunnelIdLabel = createValueLabel("pending");
        mcpHostLabel = createValueLabel("pending");
        issuerField = createReadOnlyField("");
        jwkField = createReadOnlyField("");
        audienceField = createReadOnlyField("");
        scopeField = createReadOnlyField("");
        resourceNameField = createReadOnlyField("");

        addRow(form, gbc, 0, "Tunnel ID", tunnelIdLabel);
        addRow(form, gbc, 1, "MCP host", mcpHostLabel);
        addRow(form, gbc, 2, "Issuer URI", issuerField);
        addRow(form, gbc, 3, "JWK Set URI", jwkField);
        addRow(form, gbc, 4, "Audience", audienceField);
        addRow(form, gbc, 5, "Scope", scopeField);
        addRow(form, gbc, 6, "Resource name", resourceNameField);

        card.add(header, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private JComponent buildRuntimeCard() {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout(0, 16));

        JPanel header = createCardHeader(
                "Estado del runtime",
                "Visor operativo del tunnel, la app local y el endpoint Swagger del MCP."
        );

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = defaultGbc();

        tunnelStatusLabel = createValueLabel("Tunnel: detenido");
        appStatusLabel = createValueLabel("Application: Stopped");
        swaggerLabel = createValueLabel("Swagger: pending");
        prerequisitesLabel = createValueLabel("Prerequisites: pending");

        addRow(form, gbc, 0, "Tunnel Status", tunnelStatusLabel);
        addRow(form, gbc, 1, "App Status", appStatusLabel);
        addRow(form, gbc, 2, "Swagger", swaggerLabel);
        addRow(form, gbc, 3, "Prerequisites", prerequisitesLabel);

        card.add(header, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        card.add(createInfoStrip("Arranca el runtime solo despues del login. Si algo falta, valida prerequisitos antes de abrir el MCP en un cliente externo."), BorderLayout.SOUTH);
        return card;
    }

    private JComponent buildActionBarCard() {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout(0, 16));

        JPanel header = createCardHeader(
                "Operativa Outlook MCP",
                "Todas las acciones del runtime viven aqui: validar, arrancar, parar y compartir el endpoint publico."
        );

        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        JButton validateButton = createSecondaryButton("Validar estado");
        validateButton.addActionListener(event -> runAsync(this::validatePrerequisites));
        startMcpButton = createPrimaryButton("Arrancar MCP");
        startMcpButton.setEnabled(false);
        startMcpButton.addActionListener(event -> runAsync(this::startMcp));
        stopMcpButton = createDangerButton("Parar MCP");
        stopMcpButton.setEnabled(false);
        stopMcpButton.addActionListener(event -> runAsync(this::stopMcp));
        copyMcpUrlButton = createSecondaryButton("Copiar MCP URL");
        copyMcpUrlButton.setEnabled(false);
        copyMcpUrlButton.addActionListener(event -> copyMcpUrl());
        JButton openSwaggerButton = createSecondaryButton("Abrir Swagger");
        openSwaggerButton.addActionListener(event -> openSwagger());
        JButton copySwaggerButton = createSecondaryButton("Copiar Swagger");
        copySwaggerButton.addActionListener(event -> copySwagger());

        buttons.add(validateButton);
        buttons.add(startMcpButton);
        buttons.add(stopMcpButton);
        buttons.add(copyMcpUrlButton);
        buttons.add(openSwaggerButton);
        buttons.add(copySwaggerButton);

        card.add(header, BorderLayout.NORTH);
        card.add(buttons, BorderLayout.CENTER);
        return card;
    }

    private void loadInitialDefaults() {
        machineIdField.setText(System.getenv().getOrDefault(
                "CONTROL_PLANE_MACHINE_ID",
                System.getenv().getOrDefault("COMPUTERNAME", "")
        ));
        clientVersionField.setText(System.getenv().getOrDefault("CONTROL_PLANE_CLIENT_VERSION", DEFAULT_CLIENT_VERSION));
        setTunnelStatus("Tunnel pendiente de login");
    }

    private void loginAndPrepareMcp() throws Exception {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (username.isBlank() || password.isBlank()) {
            throw new IllegalStateException("Debes introducir usuario y password antes de iniciar sesion.");
        }

        ClientLoginRequest request = new ClientLoginRequest(
                username,
                password,
                machineIdField.getText().trim(),
                clientVersionField.getText().trim()
        );
        String controlPlaneBaseUrl = resolveControlPlaneUrl();
        ClientLoginResponse loginResponse = remoteLauncherService.login(controlPlaneBaseUrl, request);
        BootstrapResponse bootstrap = loginResponse.bootstrap();

        controlPlaneSession = new ControlPlaneSession(
                controlPlaneBaseUrl,
                loginResponse.accessToken(),
                loginResponse.externalUserId(),
                request.machineId(),
                request.clientVersion(),
                loginResponse.username(),
                loginResponse.email(),
                bootstrap
        );

        applyBootstrap(bootstrap);
        loginStatusLabel.setText("Sesion preparada para " + loginResponse.username());
        loginStatusLabel.setForeground(SUCCESS);
        loginUserLabel.setText(defaultString(loginResponse.username()));
        loginExternalUserIdLabel.setText(defaultString(loginResponse.externalUserId()));
        loginAuthModeLabel.setText(bootstrap.authExposureMode().name());
        loginMcpUrlLabel.setText(defaultString(bootstrap.mcpPublicBaseUrl()));
        setTunnelStatus("Tunnel listo para arranque");
        updateLoginControls(true);
        updateMcpControls(true);
    }

    private void validatePrerequisites() {
        List<String> errors = remoteLauncherService.validatePrerequisites(controlPlaneSession);
        SwingUtilities.invokeLater(() -> {
            boolean ok = errors.isEmpty();
            prerequisitesLabel.setText(ok ? "Prerequisites: ready" : "Prerequisites: blocked");
            prerequisitesLabel.setForeground(ok ? SUCCESS : WARNING);
            startMcpButton.setEnabled(controlPlaneSession != null && applicationContext == null);
        });
        if (!errors.isEmpty()) {
            throw new IllegalStateException(String.join(System.lineSeparator(), errors));
        }
        appendLog("[launcher] Requisitos previos validados." + System.lineSeparator());
    }

    private void startMcp() throws Exception {
        if (applicationContext != null) {
            return;
        }

        BootstrapResponse bootstrap = requireBootstrap();
        if (bootstrap.authExposureMode() != AuthExposureMode.CENTRAL_AUTH) {
            throw new IllegalStateException("El launcher solo soporta CENTRAL_AUTH. Ajusta el backend para usar Keycloak central.");
        }

        remoteLauncherService.startManagedTunnel(bootstrap);
        setTunnelStatus("Tunnel activo");

        List<String> errors = remoteLauncherService.validatePrerequisites(controlPlaneSession);
        if (!errors.isEmpty()) {
            throw new IllegalStateException(String.join(System.lineSeparator(), errors));
        }

        RuntimeSettings runtimeSettings = buildRuntimeSettings();
        remoteLauncherService.writeGeneratedEnv(runtimeSettings);
        LauncherEvents.publish(new AppRuntimeState("Starting", null));

        applicationContext = CompletableFuture.supplyAsync(() ->
                        new SpringApplicationBuilder(OutlookDesktopComMcpApplication.class)
                                .headless(false)
                                .properties(runtimeSettings.toSpringProperties())
                                .run(),
                executor).join();

        SwingUtilities.invokeLater(() -> {
            stopMcpButton.setEnabled(true);
            startMcpButton.setEnabled(false);
        });
    }

    private void stopMcp() {
        stopApplication();
        remoteLauncherService.stopTunnel();
        setTunnelStatus("Tunnel detenido");
    }

    private void stopApplication() {
        ConfigurableApplicationContext context = applicationContext;
        applicationContext = null;
        if (context != null) {
            context.close();
        }
        SwingUtilities.invokeLater(() -> {
            stopMcpButton.setEnabled(false);
            startMcpButton.setEnabled(controlPlaneSession != null);
        });
    }

    private RuntimeSettings buildRuntimeSettings() {
        return remoteLauncherService.runtimeSettingsFromBootstrap(requireBootstrap());
    }

    private BootstrapResponse requireBootstrap() {
        if (controlPlaneSession == null || controlPlaneSession.bootstrap() == null) {
            throw new IllegalStateException("Primero debes iniciar sesion en la pestana Login.");
        }
        return controlPlaneSession.bootstrap();
    }

    private void applyBootstrap(BootstrapResponse bootstrap) {
        SwingUtilities.invokeLater(() -> {
            tunnelIdLabel.setText(nullToPending(bootstrap.tunnelId()));
            mcpHostLabel.setText(nullToPending(bootstrap.mcpHostname()));
            issuerField.setText(defaultString(bootstrap.issuerUri()));
            jwkField.setText(defaultString(bootstrap.jwkSetUri()));
            audienceField.setText(defaultString(bootstrap.requiredAudience()));
            scopeField.setText(defaultString(bootstrap.requiredScope()));
            resourceNameField.setText(defaultString(bootstrap.resourceName()));
            prerequisitesLabel.setText("Prerequisites: pending");
            prerequisitesLabel.setForeground(MUTED);
            startMcpButton.setEnabled(true);
        });
    }

    private void updateAppRuntimeState(AppRuntimeState state) {
        SwingUtilities.invokeLater(() -> {
            if (appStatusLabel == null || swaggerLabel == null) {
                return;
            }
            appStatusLabel.setText("Application: " + state.status());
            appStatusLabel.setForeground(resolveStatusColor(state.status()));
            swaggerLabel.setText("Swagger: " + (state.swaggerUrl() == null ? "pending" : state.swaggerUrl()));
            boolean running = "Running".equalsIgnoreCase(state.status());
            stopMcpButton.setEnabled(running);
            if (!running) {
                startMcpButton.setEnabled(controlPlaneSession != null);
            }
        });
    }

    private void updateMcpControls(boolean enabled) {
        if (startMcpButton != null) {
            startMcpButton.setEnabled(enabled);
        }
        if (!enabled && stopMcpButton != null) {
            stopMcpButton.setEnabled(false);
        }
        if (copyMcpUrlButton != null) {
            copyMcpUrlButton.setEnabled(enabled && controlPlaneSession != null);
        }
    }

    private void updateLoginControls(boolean authenticated) {
        if (loginButton != null) {
            loginButton.setEnabled(!authenticated);
        }
        if (logoutButton != null) {
            logoutButton.setEnabled(authenticated);
        }
    }

    private void setTunnelStatus(String status) {
        SwingUtilities.invokeLater(() -> {
            if (tunnelStatusLabel == null) {
                return;
            }
            tunnelStatusLabel.setText(status);
            tunnelStatusLabel.setForeground(resolveStatusColor(status));
        });
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

    private void runAsync(ThrowingRunnable action) {
        CompletableFuture.runAsync(() -> {
            try {
                action.run();
            } catch (Exception ex) {
                appendLog("[launcher] ERROR: " + ex.getMessage() + System.lineSeparator());
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE));
            }
        }, executor);
    }

    private GridBagConstraints defaultGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 7, 7, 7);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        return gbc;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent component) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        JLabel title = new JLabel(label);
        title.setFont(LABEL_FONT);
        title.setForeground(INK);
        panel.add(title, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(component, gbc);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JLabel component) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        JLabel title = new JLabel(label);
        title.setFont(LABEL_FONT);
        title.setForeground(INK);
        panel.add(title, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(component, gbc);
    }

    private void openSwagger() {
        String text = swaggerLabel.getText();
        if (text != null && text.startsWith("Swagger: http")) {
            openUri(text.substring("Swagger: ".length()));
        }
    }

    private void copySwagger() {
        String text = swaggerLabel.getText();
        if (text != null && text.startsWith("Swagger: http")) {
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection(text.substring("Swagger: ".length()).trim()), null);
            appendLog("[launcher] Swagger copiado al portapapeles." + System.lineSeparator());
        }
    }

    private void copyMcpUrl() {
        if (controlPlaneSession == null || controlPlaneSession.bootstrap() == null) {
            return;
        }
        Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection(controlPlaneSession.bootstrap().mcpPublicBaseUrl()), null);
        appendLog("[launcher] MCP URL copiada al portapapeles." + System.lineSeparator());
    }

    private void openUri(String uri) {
        if (!Desktop.isDesktopSupported()) {
            return;
        }
        try {
            Desktop.getDesktop().browse(URI.create(uri));
        } catch (Exception ex) {
            appendLog("[launcher] No he podido abrir " + uri + ": " + ex.getMessage() + System.lineSeparator());
        }
    }

    private void logout() {
        stopMcp();
        controlPlaneSession = null;
        loginStatusLabel.setText("Sesion: no autenticada");
        loginStatusLabel.setForeground(MUTED);
        loginUserLabel.setText("pending");
        loginExternalUserIdLabel.setText("pending");
        loginAuthModeLabel.setText("pending");
        loginMcpUrlLabel.setText("pending");
        tunnelIdLabel.setText("pending");
        mcpHostLabel.setText("pending");
        issuerField.setText("");
        jwkField.setText("");
        audienceField.setText("");
        scopeField.setText("");
        resourceNameField.setText("");
        prerequisitesLabel.setText("Prerequisites: pending");
        prerequisitesLabel.setForeground(MUTED);
        swaggerLabel.setText("Swagger: pending");
        appStatusLabel.setText("Application: Stopped");
        appStatusLabel.setForeground(MUTED);
        updateLoginControls(false);
        updateMcpControls(false);
        setTunnelStatus("Tunnel pendiente de login");
    }

    private String resolveControlPlaneUrl() {
        return developmentMode ? controlPlaneUrlField.getText().trim() : DEFAULT_CONTROL_PLANE_URL;
    }

    private void shutdownLauncher() {
        stopMcp();
        LauncherEvents.unregister(appStateConsumer);
        GuiLogPublisher.unregister(logConsumer);
        remoteLauncherService.shutdown();
        executor.shutdownNow();
    }

    private String nullToPending(String value) {
        return value == null || value.isBlank() ? "pending" : value;
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));
        return panel;
    }

    private JPanel createCardHeader(String title, String subtitle) {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(SECTION_FONT);
        titleLabel.setForeground(INK);
        JLabel subtitleLabel = new JLabel("<html>" + subtitle + "</html>");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitleLabel.setForeground(MUTED);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(titleLabel);
        header.add(Box.createVerticalStrut(5));
        header.add(subtitleLabel);
        return header;
    }

    private JPanel createInfoStrip(String text) {
        JPanel note = new JPanel(new BorderLayout());
        note.setBackground(BRAND_BLUE_SOFT);
        note.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(196, 208, 255), 1, true),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        JLabel label = new JLabel("<html>" + text + "</html>");
        label.setFont(new Font("SansSerif", Font.PLAIN, 13));
        label.setForeground(INK);
        note.add(label, BorderLayout.CENTER);
        return note;
    }

    private JTextField createEditableField(String value) {
        JTextField field = new JTextField(value, 28);
        styleTextField(field, true);
        return field;
    }

    private JPasswordField createPasswordField(String value) {
        JPasswordField field = new JPasswordField(value, 28);
        styleTextField(field, true);
        return field;
    }

    private JTextField createReadOnlyField(String value) {
        JTextField field = new JTextField(value, 28);
        styleTextField(field, false);
        field.setEditable(false);
        return field;
    }

    private JLabel createValueLabel(String value) {
        JLabel label = new JLabel(value);
        label.setOpaque(true);
        label.setBackground(SURFACE_ALT);
        label.setForeground(MUTED);
        label.setFont(BODY_FONT);
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        return label;
    }

    private void styleTextField(JTextField field, boolean editable) {
        field.setFont(BODY_FONT);
        field.setForeground(INK);
        field.setCaretColor(BRAND_BLUE_DARK);
        field.setBackground(editable ? Color.WHITE : SURFACE_ALT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(editable ? new Color(176, 191, 238) : BORDER, 1, true),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
    }

    private JButton createPrimaryButton(String text) {
        return styleButton(text, BRAND_BLUE, Color.WHITE);
    }

    private JButton createSecondaryButton(String text) {
        return styleButton(text, SURFACE_ALT, INK);
    }

    private JButton createDangerButton(String text) {
        return styleButton(text, DANGER, Color.WHITE);
    }

    private JButton styleButton(String text, Color background, Color foreground) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBackground(background);
        button.setForeground(foreground);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(background.darker(), 1, true),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));
        return button;
    }

    private JLabel createPillLabel(String text, Color background, Color foreground) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(background);
        label.setForeground(foreground);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(174, 147, 88), 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        return label;
    }

    private JLabel buildLogoLabel() {
        ImageIcon icon = loadImageIcon("/assets/ceac-logo-square.png");
        if (icon == null) {
            JLabel fallback = new JLabel("CEAC FP");
            fallback.setFont(new Font("Serif", Font.BOLD, 24));
            fallback.setForeground(Color.WHITE);
            return fallback;
        }
        Image scaled = icon.getImage().getScaledInstance(92, 92, Image.SCALE_SMOOTH);
        JLabel label = new JLabel(new ImageIcon(scaled));
        label.setOpaque(true);
        label.setBackground(new Color(255, 255, 255, 32));
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 60), 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        return label;
    }

    private ImageIcon loadImageIcon(String resourcePath) {
        java.net.URL resource = LauncherWindow.class.getResource(resourcePath);
        return resource == null ? null : new ImageIcon(resource);
    }

    private Color resolveStatusColor(String status) {
        if (status == null) {
            return MUTED;
        }
        String normalized = status.toLowerCase();
        if (normalized.contains("running") || normalized.contains("activo") || normalized.contains("ready")) {
            return SUCCESS;
        }
        if (normalized.contains("error") || normalized.contains("failed") || normalized.contains("detenido")) {
            return DANGER;
        }
        if (normalized.contains("starting") || normalized.contains("pending")) {
            return WARNING;
        }
        return MUTED;
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private static final class GradientPanel extends JPanel {

        private final Color from;
        private final Color to;

        private GradientPanel(Color from, Color to) {
            this.from = from;
            this.to = to;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setPaint(new GradientPaint(0, 0, from, getWidth(), getHeight(), to));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 28, 28);
            g2.dispose();
            super.paintComponent(graphics);
        }
    }
}
