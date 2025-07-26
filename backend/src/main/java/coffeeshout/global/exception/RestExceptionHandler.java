package coffeeshout.global.exception;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(Exception exception) {
        return getProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, exception);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ProblemDetail handleNoSuchElementException(NoSuchElementException exception) {
        return getProblemDetail(HttpStatus.NOT_FOUND, exception);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgumentException(IllegalArgumentException exception) {
        return getProblemDetail(HttpStatus.BAD_REQUEST, exception);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalStateException(IllegalStateException exception) {
        return getProblemDetail(HttpStatus.UNPROCESSABLE_ENTITY, exception);
    }

    private static ProblemDetail getProblemDetail(HttpStatus status, Exception exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, exception.getMessage());

        problemDetail.setProperty("timestamp", LocalDateTime.now());
        problemDetail.setProperty("exception", exception.getClass().getSimpleName());

        return problemDetail;
    }
}

