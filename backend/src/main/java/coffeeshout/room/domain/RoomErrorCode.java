package coffeeshout.room.domain;

import coffeeshout.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum RoomErrorCode implements ErrorCode {

    JOINCODE_ILLEGAL_LENGTH("코드는 5자리여야 합니다."),
    JOINCODE_ILLEGAL_CHARACTER("허용되지 않는 문자가 포함되어 있습니다."),
    PLAYER_NAME_BLANK("이름은 공백일 수 없습니다."),
    PLAYER_NAME_TOO_LONG("이름은 10자 이하여야 합니다."),
    ;

    private final String message;

    @Override
    public String getCode() {
        return this.name();
    }
}
