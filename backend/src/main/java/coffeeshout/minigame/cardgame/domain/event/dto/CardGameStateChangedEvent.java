package coffeeshout.minigame.cardgame.domain.event.dto;

import coffeeshout.minigame.cardgame.domain.CardGame;
import coffeeshout.minigame.cardgame.domain.CardGameTaskType;
import coffeeshout.room.domain.Room;

public record CardGameStateChangedEvent(
        Room room,
        CardGame cardGame,
        CardGameTaskType currentTask
) {
}
