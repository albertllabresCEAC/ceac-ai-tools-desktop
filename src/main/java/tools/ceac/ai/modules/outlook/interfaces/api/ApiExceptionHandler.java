package tools.ceac.ai.modules.outlook.interfaces.api;

import tools.ceac.ai.modules.outlook.domain.model.StatusResponse;
import tools.ceac.ai.modules.outlook.domain.exception.OutlookComException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(OutlookComException.class)
    public ResponseEntity<StatusResponse> handleOutlook(OutlookComException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new StatusResponse("ERROR", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StatusResponse> handleValidation(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest()
                .body(new StatusResponse("ERROR", ex.getBindingResult().getErrorCount() + " validation error(s)"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StatusResponse> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new StatusResponse("ERROR", ex.getMessage()));
    }
}



