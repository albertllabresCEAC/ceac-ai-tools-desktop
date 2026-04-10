package tools.ceac.ai.modules.qbid.domain.exception;

public class SessionExpiredException extends RuntimeException {
    public SessionExpiredException() {
        super("SesiÃ³n qBID expirada o invÃ¡lida. Vuelve a autenticarte.");
    }
    public SessionExpiredException(String message) {
        super(message);
    }
}



