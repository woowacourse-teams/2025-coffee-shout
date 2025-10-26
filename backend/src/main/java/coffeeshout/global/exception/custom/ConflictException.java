package coffeeshout.global.exception.custom;

import coffeeshout.global.exception.ErrorCode;

public class ConflictException extends CoffeeShoutException {

    public ConflictException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
