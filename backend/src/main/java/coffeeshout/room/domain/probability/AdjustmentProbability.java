package coffeeshout.room.domain.probability;

/*
    -10000 ~ 10000
 */
public record AdjustmentProbability(int value) {

    public static final AdjustmentProbability ZERO_PERCENT = new AdjustmentProbability(0);

    public AdjustmentProbability plus(AdjustmentProbability other) {
        return new AdjustmentProbability(this.value + other.value);
    }
}
