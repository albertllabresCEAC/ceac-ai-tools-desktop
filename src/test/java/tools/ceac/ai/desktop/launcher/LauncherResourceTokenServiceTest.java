package tools.ceac.ai.desktop.launcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

class LauncherResourceTokenServiceTest {

    private final LauncherResourceTokenService service = new LauncherResourceTokenService();

    @Test
    void shouldIssueDistinctTokensPerResourceThatMatchTheirAudience() {
        ClientLoginResponse loginResponse = new ClientLoginResponse(
                "Bearer",
                "desktop-token",
                Instant.parse("2026-04-11T12:00:00Z"),
                "user-1",
                "albert",
                "albert@test.local",
                bootstrap("outlook", "ceac-ia-tools", "outlook:tools", 8080),
                List.of(
                        new ClientMcpResourceResponse("outlook", "Outlook MCP", bootstrap("outlook", "ceac-ia-tools", "outlook:tools", 8080), null),
                        new ClientMcpResourceResponse("qbid", "QBid MCP", bootstrap("qbid", "qbid-mcp", "qbid:tools", 8082), null),
                        new ClientMcpResourceResponse("campus", "Campus MCP", bootstrap("campus", "campus-mcp", "campus:tools", 8081), null)
                )
        );

        LauncherSessionResources issued = service.issueSessionResources(loginResponse, loginResponse.resources(), "PC-1", "1.0.0");

        assertEquals(3, issued.resources().size());
        assertNotEquals(
                issued.resources().get(0).resourceToken().accessToken(),
                issued.resources().get(1).resourceToken().accessToken()
        );
        assertNotEquals(
                issued.resources().get(1).resourceToken().accessToken(),
                issued.resources().get(2).resourceToken().accessToken()
        );

        assertDecodes(issued, issued.resources().get(0));
        assertDecodes(issued, issued.resources().get(1));
        assertDecodes(issued, issued.resources().get(2));
    }

    private void assertDecodes(LauncherSessionResources issued, ClientMcpResourceResponse resource) {
        JwtDecoder decoder = LauncherJwtSupport.buildCompositeJwtDecoder(
                "https://issuer.example/realms/test",
                "https://issuer.example/realms/test/protocol/openid-connect/certs",
                resource.bootstrap().requiredAudience(),
                issued.tokenContext().issuerUri(),
                issued.tokenContext().sharedSecret()
        );
        Jwt jwt = decoder.decode(resource.resourceToken().accessToken());
        assertNotNull(jwt);
        assertEquals(resource.bootstrap().requiredAudience(), jwt.getAudience().getFirst());
        assertEquals(resource.bootstrap().requiredScope(), jwt.getClaimAsString("scope"));
    }

    private BootstrapResponse bootstrap(String resourceKey, String audience, String scope, int port) {
        return new BootstrapResponse(
                resourceKey,
                resourceKey,
                port,
                "tunnel-" + resourceKey,
                "tunnel-token",
                resourceKey + ".example.com",
                null,
                "https://" + resourceKey + ".example.com",
                null,
                "https://issuer.example/realms/test",
                "https://issuer.example/realms/test/protocol/openid-connect/certs",
                audience,
                scope,
                resourceKey,
                AuthExposureMode.CENTRAL_AUTH,
                true
        );
    }
}
