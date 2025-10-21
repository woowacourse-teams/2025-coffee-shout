package coffeeshout.room.domain;

import coffeeshout.global.exception.ErrorCode;

public enum RoomErrorCode implements ErrorCode {

    JOIN_CODE_NULL,
    JOIN_CODE_ILLEGAL_LENGTH,
    JOIN_CODE_ILLEGAL_CHARACTER,
    PLAYER_NAME_BLANK,
    PLAYER_NAME_TOO_LONG,
    ROOM_NOT_READY_TO_JOIN,
    ROOM_FULL,
    DUPLICATE_PLAYER_NAME,
    NO_EXIST_PLAYER,
    QR_CODE_GENERATION_FAILED,
    QR_CODE_UPLOAD_FAILED,
    QR_CODE_URL_SIGNING_FAILED,
    ;

    @Override
    public String getCode() {
        return this.name();
    }
}
