package coffeeshout.minigame.domain.cardgame.event.dto;

import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.room.domain.JoinCode;

public record CardSelectedEvent(JoinCode joinCode, CardGame cardGame) {
}
