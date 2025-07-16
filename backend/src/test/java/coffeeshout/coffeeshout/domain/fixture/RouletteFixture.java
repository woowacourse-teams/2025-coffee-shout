package coffeeshout.coffeeshout.domain.fixture;

import coffeeshout.coffeeshout.domain.roulette.FixedLastValueGenerator;
import coffeeshout.coffeeshout.domain.roulette.JavaRandomGenerator;
import coffeeshout.coffeeshout.domain.roulette.Roulette;

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
