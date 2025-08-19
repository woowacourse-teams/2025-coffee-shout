package coffeeshout.room.ui.response;

import coffeeshout.generator.WebsocketMessage;

@WebsocketMessage
public record GuestNameExistResponse(
        boolean exist
) {

    public static GuestNameExistResponse from(boolean existence) {
        return new GuestNameExistResponse(existence);
    }
}
