package coffeeshout.minigame.ui.response;

import coffeeshout.generator.WebsocketMessage;
import coffeeshout.minigame.domain.MiniGameType;

@WebsocketMessage
public record MiniGameStartMessage(MiniGameType miniGameType) {
}

