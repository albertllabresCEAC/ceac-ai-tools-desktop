package com.alber.outlookdesktop.launcher;

public record BootstrapRequest(
        String externalUserId,
        String machineId,
        String clientVersion
) {
}
