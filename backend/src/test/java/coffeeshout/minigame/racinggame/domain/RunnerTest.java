package coffeeshout.minigame.racinggame.domain;

import static org.assertj.core.api.Assertions.*;

import coffeeshout.fixture.PlayerFixture;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class RunnerTest {

    @Test
    void 러너의_초기_속도는_5이다() {
        // given
        final Runner runner = new Runner(PlayerFixture.게스트한스());

        // when && then
        assertThat(runner.getSpeed()).isEqualTo(5);
    }

    @Test
    void 초당_클릭수를_기반으로_속도를_조정할_수_있다() {
        // given
        final Runner runner = new Runner(PlayerFixture.게스트한스());
        final Instant firstTime = Instant.now();
        final Instant secondTime = firstTime.plusMillis(1000); // 1초 후

        // when
        runner.adjustSpeed(5, secondTime); // 1초에 5번 클릭 = 초당 5번

        // then
        assertThat(runner.getSpeed()).isEqualTo(5);
    }

    @Test
    void 초당_10번_이상_클릭하면_최대_속도가_된다() {
        // given
        final Runner runner = new Runner(PlayerFixture.게스트한스());
        final Instant firstTime = Instant.now();
        final Instant secondTime = firstTime.plusMillis(1000); // 1초 후

        // when
        runner.adjustSpeed(10, secondTime); // 1초에 10번 클릭 = 초당 10번

        // then
        assertThat(runner.getSpeed()).isEqualTo(10); // MAX_SPEED
    }

    @Test
    void 초당_클릭수가_최소_속도보다_낮으면_최소_속도가_된다() {
        // given
        final Runner runner = new Runner(PlayerFixture.게스트한스());
        final Instant firstTime = Instant.now();
        final Instant secondTime = firstTime.plusMillis(2000); // 2초 후

        // when
        runner.adjustSpeed(1, secondTime); // 2초에 1번 클릭 = 초당 0.5번

        // then
        assertThat(runner.getSpeed()).isEqualTo(1); // MIN_SPEED
    }

    @Test
    void 러너의_초기_현재_위치는_0이다() {
        // given
        final Runner runner = new Runner(PlayerFixture.게스트한스());

        // when && then
        assertThat(runner.getPosition()).isEqualTo(0);
    }

    @Test
    void 러너는_현재_속도로_움직일_수_있다() {
        // given
        final Runner runner = new Runner(PlayerFixture.게스트한스());

        // when
        runner.move();

        // then
        assertThat(runner.getPosition()).isEqualTo(5);
    }

    @Test
    void 러너가_완주_라인에_도달하면_더이상_움직이지_않는다() {
        // given
        final Runner runner = new Runner(PlayerFixture.게스트한스());
        final Instant firstTime = Instant.now();
        final Instant secondTime = firstTime.plusMillis(100);

        runner.adjustSpeed(10, secondTime); // speed = 10
        for (int i = 0; i < 100; i++) {
            runner.move();
        }

        final int position = runner.getPosition();

        // when
        runner.move();

        // then
        assertThat(runner.getPosition()).isEqualTo(position);
    }

    @Test
    void 러너가_완주했는지_확인할_수_있다() {
        // given
        final Runner runner = new Runner(PlayerFixture.게스트한스());
        final Instant firstTime = Instant.now();
        final Instant secondTime = firstTime.plusMillis(100);

        runner.adjustSpeed(10, secondTime);
        for (int i = 0; i < 100; i++) {
            runner.move();
        }

        // when && then
        assertThat(runner.isFinished()).isTrue();
    }

    @Test
    void 러너가_완주하지_않았는지_확인할_수_있다() {
        // given
        final Runner runner = new Runner(PlayerFixture.게스트한스());

        // when && then
        assertThat(runner.isFinished()).isFalse();
    }

    @Test
    void 시간_차이가_0_이하면_속도가_조정되지_않는다() {
        // given
        final Runner runner = new Runner(PlayerFixture.게스트한스());
        final Instant sameTime = Instant.now();

        // when
        runner.adjustSpeed(10, sameTime);

        // then
        assertThat(runner.getSpeed()).isEqualTo(5); // 초기 속도 유지
    }

    @Test
    void 러너가_완주하면_완주_시간이_기록된다() {
        // given
        final Runner runner = new Runner(PlayerFixture.게스트한스());
        final Instant firstTime = Instant.now();
        final Instant secondTime = firstTime.plusMillis(100);

        runner.adjustSpeed(10, secondTime);
        for (int i = 0; i < 99; i++) {
            runner.move();
        }

        // when
        runner.move();

        // then
        assertThat(runner.getFinishTime()).isNotNull();
        assertThat(runner.isFinished()).isTrue();
    }
}
