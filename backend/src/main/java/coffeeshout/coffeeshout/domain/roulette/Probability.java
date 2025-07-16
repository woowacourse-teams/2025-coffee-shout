package coffeeshout.coffeeshout.domain.roulette;

import static org.springframework.util.Assert.*;

/*
    - 확률은 100.00 형태에 100을 곱해서 소수점을 없앤 형태로 사용한다.
    - 따라서 value는 -10000 ~ 10000값을 가진다.
 */
public record Probability(Integer value) {

    private static final int MIN_PROBABILITY = -10000;
    private static final int MAX_PROBABILITY = 10000;

    public Probability {
        state(value >= MIN_PROBABILITY && value <= MAX_PROBABILITY, "확률은 -10000 ~ 10000 사이어야 합니다.");
    }

    public Probability divide(int number) {
        return new Probability(value / number);
    }

    public Probability minus(Probability other) {
        return new Probability(this.value - other.value);
    }

    public Probability plus(Probability other) {
        return new Probability(this.value + other.value);
    }

    public Probability invert(){
        return new Probability(-value);
    }
}
