package coffeeshout.minigame.cardgame.domain.cardgame.event.dto;

import coffeeshout.minigame.cardgame.domain.cardgame.CardGame;
import coffeeshout.room.domain.JoinCode;

public record CardGameStartedEvent(JoinCode joinCode, CardGame cardGame) {
}
