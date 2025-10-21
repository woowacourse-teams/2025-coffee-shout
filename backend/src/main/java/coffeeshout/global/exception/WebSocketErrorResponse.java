package coffeeshout.global.exception;

import coffeeshout.global.exception.custom.CoffeeShoutException;
import java.time.Instant;

public record WebSocketErrorResponse(
        String error,
        String message,
        String send,
        String receive,
        String timestamp
) {
    public static WebSocketErrorResponse from(Exception exception) {
        final String errorType = extractErrorType(exception);
        final String errorMessage = exception.getMessage() != null ? exception.getMessage() : "알수 없는 예외 발생";
        final String currentTimestamp = Instant.now().toString();

        return new WebSocketErrorResponse(
                errorType,
                errorMessage,
                "",
                "",
                currentTimestamp
        );
    }

    private static String extractErrorType(Exception exception) {
        if (exception instanceof CoffeeShoutException coffeeShoutException) {
            return coffeeShoutException.getErrorCode().getCode();
        }
        return exception.getClass().getSimpleName();
    }
}
