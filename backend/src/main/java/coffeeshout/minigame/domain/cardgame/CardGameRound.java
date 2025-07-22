package coffeeshout.minigame.domain.cardgame;

import lombok.Getter;

@Getter
public enum CardGameRound {

    FIRST,
    SECOND,
    END,
    ;

    public CardGameRound next() {
        final int currentRound = this.ordinal();
        CardGameRound[] values = values();
        return values[currentRound + 1];
    }

    public int toInteger() {
        return ordinal() + 1;
    }
}
