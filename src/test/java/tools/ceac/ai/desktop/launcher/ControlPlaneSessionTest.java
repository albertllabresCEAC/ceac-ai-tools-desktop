package tools.ceac.ai.desktop.launcher;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class ControlPlaneSessionTest {

    @Test
    void fallsBackToLegacyTopLevelBootstrapForOutlook() {
        BootstrapResponse legacyBootstrap = new BootstrapResponse(
                "outlook",
                "Outlook MCP",
                8080,
                "tunnel-1",
                "secret-token",
                "albert-outlook-mcp.dartmaker.com",
                null,
                "https://albert-outlook-mcp.dartmaker.com",
                null,
                "https://auth.dartmaker.com/realms/outlookdesktop-mcp",
                "https://auth.dartmaker.com/realms/outlookdesktop-mcp/protocol/openid-connect/certs",
                "outlookdesktop-mcp",
                "mcp:tools",
                "Outlook MCP",
                AuthExposureMode.CENTRAL_AUTH,
                true
        );

        ControlPlaneSession session = new ControlPlaneSession(
                "https://control.dartmaker.com",
                "desktop-token",
                "user-1",
                "machine-1",
                "1.0.0",
                "local-desktop",
                "local-desktop@example.com",
                legacyBootstrap,
                null
        );

        assertThat(session.bootstrapFor("outlook")).isEqualTo(legacyBootstrap);
        assertThat(session.bootstrapFor("qbid")).isNull();
    }

    @Test
    void prefersResourceSpecificBootstrapWhenResourcesArePresent() {
        BootstrapResponse qbidBootstrap = new BootstrapResponse(
                "qbid",
                "QBid MCP",
                8082,
                "tunnel-2",
                "secret-qbid",
                "albert-qbid-mcp.dartmaker.com",
                null,
                "https://albert-qbid-mcp.dartmaker.com",
                null,
                "https://auth.dartmaker.com/realms/outlookdesktop-mcp",
                "https://auth.dartmaker.com/realms/outlookdesktop-mcp/protocol/openid-connect/certs",
                "qbid-mcp",
                "qbid:tools",
                "QBid MCP",
                AuthExposureMode.CENTRAL_AUTH,
                true
        );

        ControlPlaneSession session = new ControlPlaneSession(
                "https://control.dartmaker.com",
                "desktop-token",
                "user-1",
                "machine-1",
                "1.0.0",
                "local-desktop",
                "local-desktop@example.com",
                null,
                List.of(new ClientMcpResourceResponse("qbid", "QBid MCP", qbidBootstrap))
        );

        assertThat(session.bootstrapFor("qbid")).isEqualTo(qbidBootstrap);
    }
}
