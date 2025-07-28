package coffeeshout.global.exception;

import coffeeshout.global.exception.custom.InvalidArgumentException;
import coffeeshout.global.exception.custom.InvalidStateException;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(Exception exception) {
        return getProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, exception, new ErrorCode() {
            @Override
            public String getCode() {
                return "INTERNAL_SERVER_ERROR";
            }

            @Override
            public String getMessage() {
                return "서버 오류가 발생했습니다.";
            }
        });
    }

    @ExceptionHandler(InvalidArgumentException.class)
    public ProblemDetail handleInvalidArgumentException(InvalidArgumentException exception) {
        return getProblemDetail(HttpStatus.BAD_REQUEST, exception, exception.getErrorCode());
    }

    @ExceptionHandler(InvalidStateException.class)
    public ProblemDetail handleIllegalFormatException(InvalidStateException exception) {
        return getProblemDetail(HttpStatus.UNPROCESSABLE_ENTITY, exception, exception.getErrorCode());
    }

    private static ProblemDetail getProblemDetail(HttpStatus status, Exception exception, ErrorCode errorCode) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, errorCode.getMessage());

        problemDetail.setProperty("errorCode", errorCode.getCode());
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        problemDetail.setProperty("exception", exception.getClass().getSimpleName());

        return problemDetail;
    }

}

