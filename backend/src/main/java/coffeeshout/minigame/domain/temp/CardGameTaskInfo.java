package coffeeshout.minigame.domain.temp;

import coffeeshout.minigame.domain.cardgame.CardGameRound;
import coffeeshout.minigame.domain.cardgame.CardGameState;

public record CardGameTaskInfo(CardGameState state, CardGameRound round) {
}
