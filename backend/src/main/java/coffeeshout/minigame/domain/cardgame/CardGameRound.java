package coffeeshout.minigame.domain.cardgame;

import static org.springframework.util.Assert.state;

import lombok.Getter;

/**
 * @deprecated 이 enum은 더 이상 사용되지 않습니다.
 * 새로운 RoundState를 사용하세요.
 */
@Deprecated
@Getter
public enum CardGameRound {
    READY,
    FIRST,
    SECOND,
    END;

    public CardGameRound next() {
        state(this.ordinal() != values().length - 1, "마지막 라운드입니다.");
        final int currentRound = this.ordinal();
        CardGameRound[] values = values();
        return values[currentRound + 1];
    }

    public int toInteger() {
        return ordinal();
    }
}
