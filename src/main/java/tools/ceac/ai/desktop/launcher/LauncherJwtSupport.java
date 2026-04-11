package tools.ceac.ai.desktop.launcher;

import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.List;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.util.StringUtils;

/**
 * Shared JWT utilities for local launcher-issued API tokens plus central Keycloak tokens.
 *
 * <p>The embedded runtimes use a composite decoder: public MCP traffic keeps the central OAuth
 * contract, while the local REST API and Swagger can also trust launcher-issued tokens for the
 * same resource audience.
 */
public final class LauncherJwtSupport {

    private LauncherJwtSupport() {
    }

    public static JwtDecoder buildCompositeJwtDecoder(
            String centralIssuerUri,
            String centralJwkSetUri,
            String requiredAudience,
            String launcherIssuerUri,
            String launcherSharedSecret
    ) {
        if (!StringUtils.hasText(centralIssuerUri)) {
            throw new IllegalStateException("mcp.remote.auth.issuer-uri is required when auth is enabled");
        }

        JwtDecoder centralDecoder = buildCentralDecoder(centralIssuerUri, centralJwkSetUri, requiredAudience);
        JwtDecoder launcherDecoder = buildLauncherDecoder(launcherIssuerUri, launcherSharedSecret, requiredAudience);

        return token -> {
            String issuer = extractIssuer(token);
            if (StringUtils.hasText(launcherIssuerUri) && launcherIssuerUri.equals(issuer) && launcherDecoder != null) {
                return launcherDecoder.decode(token);
            }
            return centralDecoder.decode(token);
        };
    }

    public static boolean hasResourceAccess(Authentication authentication, String resourceScopeAuthority) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .anyMatch(resourceScopeAuthority::equals);
    }

    private static JwtDecoder buildCentralDecoder(String issuerUri, String jwkSetUri, String requiredAudience) {
        NimbusJwtDecoder decoder = StringUtils.hasText(jwkSetUri)
                ? NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build()
                : asNimbusDecoder(JwtDecoders.fromIssuerLocation(issuerUri));
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(issuerUri),
                audienceValidator(requiredAudience)
        ));
        return decoder;
    }

    private static JwtDecoder buildLauncherDecoder(String issuerUri, String sharedSecret, String requiredAudience) {
        if (!StringUtils.hasText(issuerUri) || !StringUtils.hasText(sharedSecret)) {
            return null;
        }
        SecretKeySpec secretKey = new SecretKeySpec(sharedSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(issuerUri),
                audienceValidator(requiredAudience)
        ));
        return decoder;
    }

    private static NimbusJwtDecoder asNimbusDecoder(JwtDecoder decoder) {
        if (decoder instanceof NimbusJwtDecoder nimbusJwtDecoder) {
            return nimbusJwtDecoder;
        }
        throw new IllegalStateException("Unable to configure NimbusJwtDecoder for the configured issuer");
    }

    private static OAuth2TokenValidator<Jwt> audienceValidator(String requiredAudience) {
        return token -> {
            List<String> audience = token.getAudience();
            if (audience != null && audience.contains(requiredAudience)) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(new OAuth2Error(
                    "invalid_token",
                    "The token audience does not include the required MCP resource: " + requiredAudience,
                    null
            ));
        };
    }

    private static String extractIssuer(String token) {
        try {
            return SignedJWT.parse(token).getJWTClaimsSet().getIssuer();
        } catch (ParseException exception) {
            throw new BadJwtException("Cannot parse JWT issuer", exception);
        } catch (RuntimeException exception) {
            throw new JwtException("Cannot inspect JWT issuer", exception);
        }
    }
}
