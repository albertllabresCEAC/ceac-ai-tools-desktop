package tools.ceac.ai.modules.campus.interfaces.api;

import tools.ceac.ai.modules.campus.domain.exception.AuthenticationRequiredException;
import tools.ceac.ai.modules.campus.interfaces.desktop.CampusUiCoordinator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    private final CampusUiCoordinator campusUiCoordinator;

    public ApiExceptionHandler(CampusUiCoordinator campusUiCoordinator) {
        this.campusUiCoordinator = campusUiCoordinator;
    }

    @ExceptionHandler(AuthenticationRequiredException.class)
    public ProblemDetail handleAuth(AuthenticationRequiredException ex) {
        campusUiCoordinator.forceLoginMode("Sesion expirada o no valida. Reautentica.");
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        pd.setTitle("Authentication required");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleServer(IllegalStateException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        pd.setTitle("Internal error");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleBadRequest(IllegalArgumentException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Bad request");
        pd.setDetail(ex.getMessage());
        return pd;
    }
}


