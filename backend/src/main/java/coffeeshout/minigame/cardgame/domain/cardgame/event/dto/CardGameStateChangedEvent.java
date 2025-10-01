package coffeeshout.minigame.cardgame.domain.cardgame.event.dto;

import coffeeshout.minigame.cardgame.domain.cardgame.CardGame;
import coffeeshout.minigame.cardgame.domain.cardgame.CardGameTaskType;
import coffeeshout.room.domain.Room;

public record CardGameStateChangedEvent(
        Room room,
        CardGame cardGame,
        CardGameTaskType currentTask
) {
}
