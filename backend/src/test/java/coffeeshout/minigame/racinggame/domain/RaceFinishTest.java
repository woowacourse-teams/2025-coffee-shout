package coffeeshout.minigame.racinggame.domain;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RaceFinishTest {

    final int FINISH_LINE = 3000;
    final long INTERVAL = 100L;


    @Test
    void 남는_거리를_시간으로_환산하여_계산한다() {
        int position = 2999;
        long finishMillis = 0;

        final int speed = 30;

        int nextPosition = position + speed;
        double millisPerPosition = INTERVAL / (double) speed;
        int remainingDistance = speed - nextPosition % FINISH_LINE;
        finishMillis = (long) (remainingDistance * millisPerPosition);

        assertThat(finishMillis).isEqualTo(3);
    }

    @Test
    void 총_거리를_기준으로_정규화_한다() {
        // given
        int position = 2999;

        long finish = 0;

        final int speed = 30;

        int nextPosition = position + speed;
        finish = Math.round(FINISH_LINE * INTERVAL / nextPosition);

        assertThat(finish).isEqualTo(99);
    }
}
