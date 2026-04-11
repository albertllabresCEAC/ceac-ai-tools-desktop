package tools.ceac.ai.desktop.launcher;

import java.time.Instant;

/**
 * Token local emitido por el launcher para la API concreta de un recurso.
 *
 * <p>Este token no viene del control plane ni de Keycloak. Se usa solo para Swagger y para la API
 * local del runtime en {@code localhost}.
 */
public record ResourceAccessTokenResponse(
        String tokenType,
        String accessToken,
        Instant expiresAt,
        String scope
) {
}
