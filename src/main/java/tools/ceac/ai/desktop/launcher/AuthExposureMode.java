package tools.ceac.ai.desktop.launcher;

/**
 * Authentication topologies announced by the control-plane bootstrap.
 *
 * <p>The desktop launcher currently supports only {@link #CENTRAL_AUTH}, but the enum mirrors the
 * backend contract so the UI can reject unsupported modes explicitly.
 */
public enum AuthExposureMode {
    LOCAL_AUTH,
    CENTRAL_AUTH
}

