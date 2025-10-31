package coffeeshout.global.exception.custom;

import coffeeshout.global.exception.ErrorCode;
import lombok.Getter;

@Getter
public class CoffeeShoutException extends RuntimeException {

    private final ErrorCode errorCode;

    public CoffeeShoutException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public CoffeeShoutException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
