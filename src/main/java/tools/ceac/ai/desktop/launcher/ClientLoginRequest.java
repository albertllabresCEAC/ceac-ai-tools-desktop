package tools.ceac.ai.desktop.launcher;

public record ClientLoginRequest(
        String username,
        String password,
        String machineId,
        String clientVersion
) {
}

