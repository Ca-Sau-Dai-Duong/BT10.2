package vn.iotstar.exceptions;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleSecurityException(Exception exception) {
        exception.printStackTrace();

        if (exception instanceof BadCredentialsException) {
            return build(401, exception.getMessage(), "The username or password is incorrect");
        }
        if (exception instanceof UsernameNotFoundException) {
            return build(401, exception.getMessage(), "The user of this token no longer exists");
        }
        if (exception instanceof AccountStatusException) {
            return build(403, exception.getMessage(), "The account is locked");
        }
        if (exception instanceof AccessDeniedException) {
            return build(403, exception.getMessage(), "You are not authorized to access this resource");
        }
        if (exception instanceof ExpiredJwtTokenException) {
            return build(401, exception.getMessage(), "The JWT token has expired");
        }
        if (exception instanceof InvalidJwtException) {
            return build(401, exception.getMessage(), "The JWT signature/format is invalid");
        }
        if (exception instanceof ResponseStatusException rse) {
            return build(rse.getStatusCode().value(), rse.getReason(), rse.getReason());
        }
        return build(500, exception.getMessage(), "Unknown internal server error.");
    }

    private ProblemDetail build(int status, String detail, String description) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(status), detail);
        pd.setProperty("description", description);
        return pd;
    }
}
