package coffeeshout.room.domain.player;

import coffeeshout.global.exception.ErrorCode;

public enum ColorErrorCode implements ErrorCode {

    NO_AVAILABLE_COLOR,
    INVALID_COLOR_INDEX,
    ;

    @Override
    public String getCode() {
        return this.name();
    }
}
