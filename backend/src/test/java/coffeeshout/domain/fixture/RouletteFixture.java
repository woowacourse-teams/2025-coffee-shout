package coffeeshout.domain.fixture;

import coffeeshout.domain.FixedLastValueGenerator;
import coffeeshout.domain.JavaRandomGenerator;
import coffeeshout.domain.Roulette;

public final class RouletteFixture {

    private RouletteFixture() {
    }

    public static Roulette 고정_끝값_반환() {
        return new Roulette(new FixedLastValueGenerator());
    }

    public static Roulette 랜덤_반환() {
        return new Roulette(new JavaRandomGenerator());
    }
}
