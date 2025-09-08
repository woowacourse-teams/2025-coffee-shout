package coffeeshout.room.ui.response;

import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;

public record RoomCreateResponse(
        String joinCode,
        String qrCodeUrl
) {

    public static RoomCreateResponse from(Room room) {
        final JoinCode joinCode = room.getJoinCode();
        return new RoomCreateResponse(
                joinCode.getValue(),
                joinCode.getQrCodeUrl()
        );
    }
}
