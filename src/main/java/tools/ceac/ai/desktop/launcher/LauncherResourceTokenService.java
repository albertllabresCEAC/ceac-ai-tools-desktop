package tools.ceac.ai.desktop.launcher;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Issues local per-resource JWTs that the embedded runtimes can validate without depending on
 * external Keycloak resource issuance.
 *
 * <p>The resulting JWTs are operational tokens for the local REST API and Swagger only. They do
 * not replace the central OAuth contract announced by the public MCP endpoints.
 */
public class LauncherResourceTokenService {

    private static final String LOCAL_ISSUER_PREFIX = "ceac-ai-tools://launcher/session";
    private static final String TOKEN_TYPE = "Bearer";
    private static final long LOCAL_TOKEN_TTL_SECONDS = 24 * 60 * 60;

    public LauncherSessionResources issueSessionResources(
            ClientLoginResponse loginResponse,
            List<ClientMcpResourceResponse> resources,
            String machineId,
            String clientVersion
    ) {
        LauncherTokenContext context = new LauncherTokenContext(
                LOCAL_ISSUER_PREFIX + "/" + UUID.randomUUID(),
                randomSharedSecret()
        );

        List<ClientMcpResourceResponse> enrichedResources = resources.stream()
                .map(resource -> withIssuedToken(loginResponse, resource, context, machineId, clientVersion))
                .toList();

        return new LauncherSessionResources(context, enrichedResources);
    }

    private ClientMcpResourceResponse withIssuedToken(
            ClientLoginResponse loginResponse,
            ClientMcpResourceResponse resource,
            LauncherTokenContext context,
            String machineId,
            String clientVersion
    ) {
        if (resource == null || resource.bootstrap() == null) {
            return resource;
        }
        return new ClientMcpResourceResponse(
                resource.resourceKey(),
                resource.displayName(),
                resource.bootstrap(),
                issueResourceToken(loginResponse, resource.bootstrap(), context, machineId, clientVersion)
        );
    }

    private ResourceAccessTokenResponse issueResourceToken(
            ClientLoginResponse loginResponse,
            BootstrapResponse bootstrap,
            LauncherTokenContext context,
            String machineId,
            String clientVersion
    ) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(LOCAL_TOKEN_TTL_SECONDS);

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(context.issuerUri())
                .subject(firstNonBlank(loginResponse.externalUserId(), loginResponse.username(), "desktop-user"))
                .audience(bootstrap.requiredAudience())
                .issueTime(java.util.Date.from(issuedAt))
                .expirationTime(java.util.Date.from(expiresAt))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", bootstrap.requiredScope())
                .claim("preferred_username", loginResponse.username())
                .claim("email", loginResponse.email())
                .claim("resource_key", bootstrap.resourceKey())
                .claim("resource_name", bootstrap.resourceName())
                .claim("machine_id", machineId)
                .claim("client_version", clientVersion)
                .build();

        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        try {
            jwt.sign(new MACSigner(context.sharedSecret().getBytes(StandardCharsets.UTF_8)));
        } catch (JOSEException exception) {
            throw new IllegalStateException("No he podido firmar el token local del launcher.", exception);
        }

        return new ResourceAccessTokenResponse(TOKEN_TYPE, jwt.serialize(), expiresAt, bootstrap.requiredScope());
    }

    private String randomSharedSecret() {
        byte[] bytes = new byte[32];
        ThreadLocalRandom.current().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
