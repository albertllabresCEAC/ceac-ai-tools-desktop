package tools.ceac.ai.desktop.launcher;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import org.junit.jupiter.api.Test;
import tools.ceac.ai.security.ClientAccessLevel;

class ControlPlaneSessionTest {

    @Test
    void fallsBackToLegacyTopLevelBootstrapForOutlook() {
        BootstrapResponse legacyBootstrap = new BootstrapResponse(
                "outlook",
                "Outlook MCP",
                8080,
                "tunnel-1",
                "secret-token",
                "albert-outlook.dartmaker.com",
                null,
                "https://albert-outlook.dartmaker.com",
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
                ClientAccessLevel.READ_WRITE,
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
                "albert-qbid.dartmaker.com",
                null,
                "https://albert-qbid.dartmaker.com",
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
                ClientAccessLevel.READ_WRITE,
                null,
                List.of(new ClientMcpResourceResponse("qbid", "QBid MCP", qbidBootstrap, null)),
                null,
                null
        );

        assertThat(session.bootstrapFor("qbid")).isEqualTo(qbidBootstrap);
    }

    @Test
    void resolvesReadOnlyAccessLevelFromDesktopTokenWhenResponseOmitsIt() {
        ControlPlaneSession session = new ControlPlaneSession(
                "https://control.dartmaker.com",
                jwtWithRoles("DESKTOP_CLIENT", "CEAC_READ_ONLY"),
                "user-1",
                "machine-1",
                "1.0.0",
                "laura",
                "laura@dartmaker.local",
                null,
                null,
                null,
                null,
                null
        );

        assertThat(session.accessLevel()).isEqualTo(ClientAccessLevel.READ_ONLY);
        assertThat(session.allowsWrites()).isFalse();
    }

    private String jwtWithRoles(String... roles) {
        String header = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8));
        String roleArray = java.util.Arrays.stream(roles)
                .map(role -> "\"" + role + "\"")
                .collect(java.util.stream.Collectors.joining(","));
        String payloadJson = "{\"realm_access\":{\"roles\":[" + roleArray + "]}}";
        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        return header + "." + payload + ".";
    }
}
