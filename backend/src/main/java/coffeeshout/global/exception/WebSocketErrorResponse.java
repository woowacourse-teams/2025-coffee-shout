package coffeeshout.global.exception;

import coffeeshout.global.exception.custom.CoffeeShoutException;
import java.time.Instant;

public record WebSocketErrorResponse(
        String error,
        String message,
        String timestamp
) {
    public static WebSocketErrorResponse from(Throwable throwable) {
        final String errorType = extractErrorType(throwable);
        final String errorMessage = extractErrorMessage(throwable);
        final String currentTimestamp = Instant.now().toString();

        return new WebSocketErrorResponse(
                errorType,
                errorMessage,
                currentTimestamp
        );
    }

    private static String extractErrorMessage(Throwable exception) {
        if (exception instanceof CoffeeShoutException coffeeShoutException) {
            return coffeeShoutException.getMessage();
        }
        return "알 수 없는 예외 발생";
    }


    private static String extractErrorType(Throwable exception) {
        if (exception instanceof CoffeeShoutException coffeeShoutException) {
            return coffeeShoutException.getErrorCode().getCode();
        }
        return exception.getClass().getSimpleName();
    }
}
