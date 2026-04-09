package com.alber.outlookdesktop.launcher;

public record BootstrapRequest(
        String externalUserId,
        String resourceKey,
        String machineId,
        String clientVersion
) {
}
