package coffeeshout.coffeeshout.domain.roulette;

public class FixedLastValueGenerator implements RandomGenerator {

    @Override
    public int nextInt(final int origin, final int bound) {
        return bound;
    }
}
