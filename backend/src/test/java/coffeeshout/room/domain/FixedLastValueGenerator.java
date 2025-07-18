package coffeeshout.room.domain;

public class FixedLastValueGenerator implements RandomGenerator {

    @Override
    public int nextInt(int origin, int bound) {
        return bound;
    }
}
