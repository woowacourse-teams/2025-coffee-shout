package coffeeshout.minigame.domain.dto;

import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.room.domain.JoinCode;

public record CardGameStateChangeEvent(JoinCode joinCode, CardGame cardGame) {
}
