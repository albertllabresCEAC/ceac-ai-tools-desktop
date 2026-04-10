package tools.ceac.ai.mcp.qbid.exception;

public class SessionExpiredException extends RuntimeException {
    public SessionExpiredException() {
        super("Sesión qBID expirada o inválida. Vuelve a autenticarte.");
    }
    public SessionExpiredException(String message) {
        super(message);
    }
}

