package coffeeshout.minigame.cardgame.domain;

import coffeeshout.minigame.cardgame.domain.cardgame.CardGame;
import coffeeshout.minigame.cardgame.domain.cardgame.card.CardGameRandomDeckGenerator;
import coffeeshout.minigame.racinggame.domain.RacingGame;
import coffeeshout.room.domain.Playable;
import java.util.Objects;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum MiniGameType {

    CARD_GAME,
    RACING_GAME,
    ;

    public Playable createMiniGame(String joinCode) {
        switch (this) {
            case CARD_GAME:
                Objects.requireNonNull(joinCode, "joinCode must not be null");
                final long seed = Integer.toUnsignedLong(joinCode.hashCode());
                return new CardGame(new CardGameRandomDeckGenerator(), seed);
            case RACING_GAME:
                return new RacingGame();
        }

        throw new IllegalArgumentException("선택한 게임이 존재하지 않습니다.");
    }
}
