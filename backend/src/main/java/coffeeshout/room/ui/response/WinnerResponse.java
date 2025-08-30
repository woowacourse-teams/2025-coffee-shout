package coffeeshout.room.ui.response;

import coffeeshout.room.domain.player.Winner;
import generator.annotaions.WebSocketMessage;

@WebSocketMessage
public record WinnerResponse(
        String playerName,
        Integer colorIndex,
        Integer randomAngle
) {

    public static WinnerResponse from(Winner winner) {
        return new WinnerResponse(
                winner.name().value(),
                winner.colorIndex(),
                winner.randomAngle()
        );
    }
}
