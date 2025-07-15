package coffeeshout.domain;

import lombok.Getter;

@Getter
public enum CardGameRound {
    ONE(1), TWO(2), END(-1);

    private Integer value;

    CardGameRound(Integer value) {
        this.value = value;
    }

    public CardGameRound next() {
        final Integer ordinal = this.ordinal();
        CardGameRound[] values = values();
        return values[ordinal + 1];
    }
}
