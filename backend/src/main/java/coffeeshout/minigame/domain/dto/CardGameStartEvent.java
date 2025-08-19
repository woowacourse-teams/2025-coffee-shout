package coffeeshout.minigame.domain.dto;

import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.room.domain.JoinCode;

public record CardGameStartEvent(JoinCode joinCode, CardGame cardGame) {
}
