package coffeeshout.room.ui.response;

import coffeeshout.generator.WebsocketMessage;

@WebsocketMessage
public record JoinCodeExistResponse(
        boolean exist
) {

    public static JoinCodeExistResponse from(boolean existence) {
        return new JoinCodeExistResponse(existence);
    }
}
