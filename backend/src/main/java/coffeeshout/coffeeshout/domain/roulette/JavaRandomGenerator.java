package coffeeshout.coffeeshout.domain.roulette;

import java.util.Random;

public class JavaRandomGenerator implements RandomGenerator {
    private final Random random = new Random();

    @Override
    public int nextInt(int bound) {
        return random.nextInt(bound);
    }
}