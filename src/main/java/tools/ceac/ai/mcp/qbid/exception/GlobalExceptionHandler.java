package tools.ceac.ai.mcp.qbid.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SessionExpiredException.class)
    public ResponseEntity<Map<String, Object>> handleSessionExpired(SessionExpiredException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorBody(401, ex.getMessage()));
    }

    @ExceptionHandler(QbidParseException.class)
    public ResponseEntity<Map<String, Object>> handleParseFailed(QbidParseException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorBody(502, ex.getMessage()));
    }

    // No handler found: let Spring Boot return 404 normally.
    // Previously this was caught by handleGeneric() and returned 500.
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResourceFound(NoResourceFoundException ex) {
        System.out.printf("[GlobalExceptionHandler] 404 NoResourceFoundException: %s%n", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody(404, ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        System.out.printf("[GlobalExceptionHandler] 500 %s: %s%n",
                ex.getClass().getName(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorBody(500, "Error interno: " + ex.getMessage()));
    }

    private Map<String, Object> errorBody(int status, String message) {
        return Map.of(
                "status", status,
                "error", message,
                "timestamp", LocalDateTime.now().toString()
        );
    }
}

