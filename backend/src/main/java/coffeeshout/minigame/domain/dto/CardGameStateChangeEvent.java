package coffeeshout.minigame.domain.dto;

import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.CardGameTaskType;
import coffeeshout.room.domain.Room;

public record CardGameStateChangeEvent(
        Room room,
        CardGame cardGame,
        CardGameTaskType currentTask
) {
}
