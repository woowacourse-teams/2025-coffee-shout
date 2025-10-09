package coffeeshout.minigame.event.dto;

import coffeeshout.minigame.cardgame.domain.CardGame;
import coffeeshout.room.domain.JoinCode;

public record CardSelectedEvent(JoinCode joinCode, CardGame cardGame) {
}
