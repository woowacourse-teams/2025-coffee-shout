package coffeeshout.global.exception;

import static coffeeshout.global.log.LogAspect.NOTIFICATION_MARKER;

import coffeeshout.global.exception.custom.InvalidArgumentException;
import coffeeshout.global.exception.custom.InvalidStateException;
import coffeeshout.global.exception.custom.NotExistElementException;
import coffeeshout.global.exception.custom.QRCodeGenerationException;
import coffeeshout.global.exception.custom.StorageServiceException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
@Slf4j
public class RestExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(
            Exception exception,
            HttpServletRequest request
    ) {
        logError(exception, request);
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

    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleNoResourceFoundException(
            NoResourceFoundException exception,
            HttpServletRequest request
    ) {
        logWarning(exception, request);
        return getProblemDetail(HttpStatus.NOT_FOUND, exception, new ErrorCode() {
            @Override
            public String getCode() {
                return "RESOURCE_NOT_FOUND";
            }

            @Override
            public String getMessage() {
                return "요청한 리소스를 찾을 수 없습니다.";
            }
        });
    }

    @ExceptionHandler(InvalidArgumentException.class)
    public ProblemDetail handleInvalidArgumentException(
            InvalidArgumentException exception,
            HttpServletRequest request
    ) {
        logWarning(exception, request);
        return getProblemDetail(HttpStatus.BAD_REQUEST, exception, exception.getErrorCode());
    }

    @ExceptionHandler(InvalidStateException.class)
    public ProblemDetail handleInvalidStateException(
            InvalidStateException exception,
            HttpServletRequest request
    ) {
        logError(exception, request);
        return getProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, exception, exception.getErrorCode());
    }

    @ExceptionHandler(NotExistElementException.class)
    public ProblemDetail handleNotExistElementException(
            NotExistElementException exception,
            HttpServletRequest request
    ) {
        logWarning(exception, request);
        return getProblemDetail(HttpStatus.NOT_FOUND, exception, exception.getErrorCode());
    }

    @ExceptionHandler(QRCodeGenerationException.class)
    public ProblemDetail handleQRCodeGenerationException(QRCodeGenerationException exception,
                                                         HttpServletRequest request) {
        logError(exception, request);
        return getProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, exception, exception.getErrorCode());
    }

    @ExceptionHandler(StorageServiceException.class)
    public ProblemDetail handleStorageServiceException(StorageServiceException exception,
                                                       HttpServletRequest request) {
        logError(exception, request);
        return getProblemDetail(HttpStatus.SERVICE_UNAVAILABLE, exception, exception.getErrorCode());
    }

    private static ProblemDetail getProblemDetail(HttpStatus status, Exception exception, ErrorCode errorCode) {
        final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, errorCode.getMessage());

        problemDetail.setProperty("errorCode", errorCode.getCode());
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        problemDetail.setProperty("exception", exception.getClass().getSimpleName());

        return problemDetail;
    }

    private void logError(
            final Exception e,
            final HttpServletRequest request
    ) {
        final String logMessage = String.format(
                "method=%s uri=%s exception=%s message=%s",
                request.getMethod(),
                request.getRequestURI(),
                e.getClass().getSimpleName(),
                e.getMessage()
        );
        log.error(NOTIFICATION_MARKER, logMessage, e);
    }

    private void logWarning(
            final Exception e,
            final HttpServletRequest request
    ) {
        final String logMessage = String.format(
                "method=%s uri=%s exception=%s message=%s",
                request.getMethod(),
                request.getRequestURI(),
                e.getClass().getSimpleName(),
                e.getMessage()
        );
        log.warn(logMessage);
    }
}

