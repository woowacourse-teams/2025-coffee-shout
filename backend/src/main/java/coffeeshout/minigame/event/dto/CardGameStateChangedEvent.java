package coffeeshout.minigame.event.dto;

import coffeeshout.minigame.cardgame.domain.CardGame;
import coffeeshout.minigame.cardgame.domain.CardGameTaskType;
import coffeeshout.room.domain.Room;
import java.util.UUID;

public record CardGameStateChangedEvent(
        UUID eventId,
        Room room,
        CardGame cardGame,
        CardGameTaskType currentTask
) {

    public CardGameStateChangedEvent(Room room, CardGame cardGame, CardGameTaskType cardGameTaskType) {
        this(UUID.randomUUID(), room, cardGame, cardGameTaskType);
    }
}
