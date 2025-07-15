package coffeeshout.coffeeshout.domain.roulette;

public class FixedRandomGenerator implements RandomGenerator {
    @Override
    public int nextInt(int bound) {
        return bound - 1;
    }
}
