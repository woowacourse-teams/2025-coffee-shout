package coffeeshout.minigame.domain.cardgame;

import lombok.Getter;

@Getter
public enum CardGameRound {

    FIRST(1),
    SECOND(2),
    END(-1);

    final private Integer value;

    CardGameRound(Integer value) {
        this.value = value;
    }

    public CardGameRound next() {
        final int currentRound = this.ordinal();
        CardGameRound[] values = values();
        return values[currentRound + 1];
    }
}
