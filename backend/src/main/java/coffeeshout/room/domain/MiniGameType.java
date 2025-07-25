package coffeeshout.room.domain;

import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.CardGameRandomDeckGenerator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum MiniGameType {

    CARD_GAME,
    ;

    public Playable createMiniGame() {
        switch (this) {
            case CARD_GAME:
                return new CardGame(new CardGameRandomDeckGenerator());
        }

        throw new IllegalArgumentException("선택한 게임이 존재하지 않습니다.");
    }
}
