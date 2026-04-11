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
                "https://auth.dartmaker.com/realms/ceac-ia-tools",
                "https://auth.dartmaker.com/realms/ceac-ia-tools/protocol/openid-connect/certs",
                "ceac-ia-tools",
                "outlook:tools",
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
                "albert",
                "albert.llabres@ceacfp.es",
                legacyBootstrap,
                null,
                null,
                null
        );

        assertThat(session.bootstrapFor("outlook")).isEqualTo(legacyBootstrap);
        assertThat(session.resourceFor("outlook")).isNotNull();
        assertThat(session.resourceFor("outlook").bootstrap()).isEqualTo(legacyBootstrap);
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
                "https://auth.dartmaker.com/realms/ceac-ia-tools",
                "https://auth.dartmaker.com/realms/ceac-ia-tools/protocol/openid-connect/certs",
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
                "albert",
                "albert.llabres@ceacfp.es",
                null,
                List.of(new ClientMcpResourceResponse("qbid", "QBid MCP", qbidBootstrap, null)),
                null,
                null
        );

        assertThat(session.bootstrapFor("qbid")).isEqualTo(qbidBootstrap);
    }
}
