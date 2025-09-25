package coffeeshout.minigame.domain;

import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.card.CardGameRandomDeckGenerator;
import coffeeshout.room.domain.Playable;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum MiniGameType {

    CARD_GAME,
    ;

    public Playable createMiniGame(String joinCode) {
        switch (this) {
            case CARD_GAME:
                long seed = joinCode.hashCode();
                return new CardGame(new CardGameRandomDeckGenerator(), seed);
        }

        throw new IllegalArgumentException("선택한 게임이 존재하지 않습니다.");
    }
}
