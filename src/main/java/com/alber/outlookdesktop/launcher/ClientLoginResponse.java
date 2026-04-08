package com.alber.outlookdesktop.launcher;

import java.time.Instant;

public record ClientLoginResponse(
        String tokenType,
        String accessToken,
        Instant expiresAt,
        String externalUserId,
        String username,
        String email,
        BootstrapResponse bootstrap
) {
}
