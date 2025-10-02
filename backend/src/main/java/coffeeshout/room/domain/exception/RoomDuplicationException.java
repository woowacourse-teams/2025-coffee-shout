package coffeeshout.room.domain.exception;

public class RoomDuplicationException extends RuntimeException {
    public RoomDuplicationException(String message) {
        super(message);
    }
}
