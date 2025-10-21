package coffeeshout.global.exception;

public enum GlobalErrorCode implements ErrorCode {

    NOT_EXIST,
    ;

    @Override
    public String getCode() {
        return this.name();
    }
}
