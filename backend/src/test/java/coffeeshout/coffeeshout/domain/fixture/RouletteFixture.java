package coffeeshout.coffeeshout.domain.fixture;

import coffeeshout.coffeeshout.domain.FixedLastValueGenerator;
import coffeeshout.coffeeshout.domain.JavaRandomGenerator;
import coffeeshout.coffeeshout.domain.Roulette;

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
