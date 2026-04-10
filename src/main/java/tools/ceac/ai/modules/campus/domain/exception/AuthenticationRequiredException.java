package tools.ceac.ai.modules.campus.domain.exception;

/**
 * Raised when campus session is missing or expired.
 */
public class AuthenticationRequiredException extends RuntimeException {
    public AuthenticationRequiredException(String message) {
        super(message);
    }
}


