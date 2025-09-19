package coffeeshout.minigame.domain.cardgame.event.dto;

import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.CardGameTaskType;
import coffeeshout.room.domain.Room;

public record CardGameStateChangedEvent(
        Room room,
        CardGame cardGame,
        CardGameTaskType currentTask
) {
}
