package coffeeshout.room.ui.response;

import generator.annotaions.WebSocketMessage;

@WebSocketMessage
public record GuestNameExistResponse(
        boolean exist
) {

    public static GuestNameExistResponse from(boolean existence) {
        return new GuestNameExistResponse(existence);
    }
}
