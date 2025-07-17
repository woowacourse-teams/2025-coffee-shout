package coffeeshout.coffeeshout.domain;

import java.util.Random;

public class JavaRandomGenerator implements RandomGenerator {

    private final Random random = new Random();

    @Override
    public int nextInt(int origin, int bound) {
        return random.nextInt(origin, bound + 1);
    }
}
