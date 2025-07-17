package coffeeshout.coffeeshout.domain;

import coffeeshout.coffeeshout.domain.RandomGenerator;

public class FixedLastValueGenerator implements RandomGenerator {

    @Override
    public int nextInt(int origin, int bound) {
        return bound;
    }
}
