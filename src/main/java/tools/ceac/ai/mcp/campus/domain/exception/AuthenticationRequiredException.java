package tools.ceac.ai.mcp.campus.domain.exception;

/**
 * Raised when campus session is missing or expired.
 */
public class AuthenticationRequiredException extends RuntimeException {
    public AuthenticationRequiredException(String message) {
        super(message);
    }
}
