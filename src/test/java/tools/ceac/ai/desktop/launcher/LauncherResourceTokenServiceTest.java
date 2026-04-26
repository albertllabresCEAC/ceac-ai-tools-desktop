package tools.ceac.ai.desktop.launcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import tools.ceac.ai.security.ClientAccessLevel;

class LauncherResourceTokenServiceTest {

    private final LauncherResourceTokenService service = new LauncherResourceTokenService();

    @Test
    void shouldIssueDistinctTokensPerResourceThatMatchTheirAudience() {
        ClientLoginResponse loginResponse = new ClientLoginResponse(
                "Bearer",
                "desktop-token",
                Instant.now().plusSeconds(3600),
                "user-1",
                "albert",
                "albert@test.local",
                ClientAccessLevel.READ_WRITE,
                bootstrap("outlook", "ceac-ia-tools", "outlook:tools", 8080),
                List.of(
                        new ClientMcpResourceResponse("outlook", "Outlook MCP", bootstrap("outlook", "ceac-ia-tools", "outlook:tools", 8080), null),
                        new ClientMcpResourceResponse("qbid", "QBid MCP", bootstrap("qbid", "qbid-mcp", "qbid:tools", 8082), null),
                        new ClientMcpResourceResponse("campus", "Campus MCP", bootstrap("campus", "campus-mcp", "campus:tools", 8081), null),
                        new ClientMcpResourceResponse("trello", "Trello MCP", bootstrap("trello", "trello-mcp", "trello:tools", 8083), null)
                )
        );

        LauncherSessionResources issued = service.issueSessionResources(loginResponse, loginResponse.resources(), "PC-1", "1.0.0");

        assertEquals(4, issued.resources().size());
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
        assertDecodes(issued, issued.resources().get(3));
    }

    @Test
    void shouldPreserveReadOnlyAccessWhenControlPlaneResponseOmitsExplicitAccessLevel() {
        ClientLoginResponse loginResponse = new ClientLoginResponse(
                "Bearer",
                jwtWithRoles("DESKTOP_CLIENT", "CEAC_READ_ONLY"),
                Instant.now().plusSeconds(3600),
                "user-1",
                "laura",
                "laura@test.local",
                null,
                bootstrap("outlook", "ceac-ia-tools", "outlook:tools", 8080),
                List.of(new ClientMcpResourceResponse(
                        "outlook",
                        "Outlook MCP",
                        bootstrap("outlook", "ceac-ia-tools", "outlook:tools", 8080),
                        null
                ))
        );

        LauncherSessionResources issued = service.issueSessionResources(loginResponse, loginResponse.resources(), "PC-1", "1.0.0");
        JwtDecoder decoder = LauncherJwtSupport.buildCompositeJwtDecoder(
                "https://issuer.example/realms/test",
                "https://issuer.example/realms/test/protocol/openid-connect/certs",
                "ceac-ia-tools",
                issued.tokenContext().issuerUri(),
                issued.tokenContext().sharedSecret()
        );
        Jwt jwt = decoder.decode(issued.resources().getFirst().resourceToken().accessToken());

        assertEquals("READ_ONLY", jwt.getClaimAsString("ceac_access_level"));
        assertEquals(Boolean.FALSE, jwt.getClaim("ceac_write_access"));
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
