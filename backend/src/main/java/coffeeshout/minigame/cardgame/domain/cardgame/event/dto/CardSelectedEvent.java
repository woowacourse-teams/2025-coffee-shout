package coffeeshout.minigame.cardgame.domain.cardgame.event.dto;

import coffeeshout.minigame.cardgame.domain.cardgame.CardGame;
import coffeeshout.room.domain.JoinCode;

public record CardSelectedEvent(JoinCode joinCode, CardGame cardGame) {
}
