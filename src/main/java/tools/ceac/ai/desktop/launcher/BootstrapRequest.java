package tools.ceac.ai.desktop.launcher;

public record BootstrapRequest(
        String externalUserId,
        String resourceKey,
        String machineId,
        String clientVersion
) {
}

