package coffeeshout.room.ui.response;

import generator.annotaions.WebSocketMessage;

@WebSocketMessage
public record JoinCodeExistResponse(
        boolean exist
) {

    public static JoinCodeExistResponse from(boolean existence) {
        return new JoinCodeExistResponse(existence);
    }
}
