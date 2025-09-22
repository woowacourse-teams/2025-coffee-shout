package coffeeshout.minigame.domain.cardgame.event.dto;

import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.room.domain.JoinCode;

public record CardGameStartedEvent(JoinCode joinCode, CardGame cardGame) {
}
