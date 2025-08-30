package coffeeshout.minigame.ui.response;

import coffeeshout.minigame.domain.MiniGameType;
import generator.annotaions.WebSocketMessage;

@WebSocketMessage
public record MiniGameStartMessage(MiniGameType miniGameType) {
}

