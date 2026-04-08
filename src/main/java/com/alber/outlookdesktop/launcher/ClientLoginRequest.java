package com.alber.outlookdesktop.launcher;

public record ClientLoginRequest(
        String username,
        String password,
        String machineId,
        String clientVersion
) {
}
