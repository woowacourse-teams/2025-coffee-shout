package coffeeshout.minigame.racinggame.domain;

import static org.assertj.core.api.Assertions.*;

import coffeeshout.fixture.PlayerFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RunnerTest {

    @Test
    void 러너의_초기_속도는_5이다() {
        // given
        final Runner runner = new Runner(PlayerFixture.게스트한스());

        // when && then
        assertThat(runner.getSpeed()).isEqualTo(5);
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 5, 6, 7})
    void 러너의_속도를_조절할_수_있다(int adjustmentSpeed) {
        // given
        final Runner runner = new Runner(PlayerFixture.게스트한스());

        // when
        final int beforeSpeed = runner.getSpeed();
        runner.adjustSpeed(adjustmentSpeed);

        // then
        assertThat(runner.getSpeed()).isEqualTo(beforeSpeed + adjustmentSpeed);
    }

    @Test
    void 러너의_초기_현재_위치는_0이다() {
        // given
        final Runner runner = new Runner(PlayerFixture.게스트한스());

        // when && then
        assertThat(runner.getPosition()).isEqualTo(0);
    }

    @Test
    void 러너는_움직_수_있다() {
        // given
        final Runner runner = new Runner(PlayerFixture.게스트한스());

        // when
        runner.move(5);

        // then
        assertThat(runner.getPosition()).isEqualTo(5);
    }

    @Test
    void 러너가_뒤로_움직이려하면_예외가_발생한다() {
        // given
        final Runner runner = new Runner(PlayerFixture.게스트한스());

        // when && then
        assertThatThrownBy(() -> runner.move(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("뒤로는 움직일 수 없습니다.");
    }

    @Test
    void 러너가_이미_완주했는데_움직이려하면_예외가_발생한다() {
        // given
        final Runner runner = new Runner(PlayerFixture.게스트한스());
        runner.move(999);

        // when && then
        assertThatThrownBy(() -> runner.move(1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 완주하여 더이상 움직일 수 없습니다.");
    }

    @ParameterizedTest
    @ValueSource(ints = {95, 96})
    void 러너의_속도가_100_이상이_되면_예외가_발생한다(int adjustmentSpeed) {
        // given
        final Runner runner = new Runner(PlayerFixture.게스트한스());

        // when && then
        assertThatThrownBy(() -> runner.adjustSpeed(adjustmentSpeed))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("속도는 0 ~ 100 사이여야 합니다.");
    }

    @ParameterizedTest
    @ValueSource(ints = {-5, -6})
    void 러너의_속도가_5_이하가_되면_예외가_발생한다(int adjustmentSpeed) {
        // given
        final Runner runner = new Runner(PlayerFixture.게스트한스());

        // when && then
        assertThatThrownBy(() -> runner.adjustSpeed(adjustmentSpeed))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("속도는 0 ~ 100 사이여야 합니다.");
    }

    @Test
    void 탭_커맨드를_처리하여_속도를_조정할_수_있다() {
        // given
        final Runner runner = new Runner(PlayerFixture.게스트한스());
        final TapCommand tapCommand = new TapCommand(PlayerFixture.게스트한스(), 10);

        // when
        runner.processTapCommand(tapCommand);

        // then
        assertThat(runner.getSpeed()).isEqualTo(15);
    }

    @Test
    void 러너와_탭_커맨드의_플레이어가_다르면_예외가_발생한다() {
        // given
        final Runner runner = new Runner(PlayerFixture.게스트한스());
        final TapCommand tapCommand = new TapCommand(PlayerFixture.호스트피케이(), 10);

        // when && then
        assertThatThrownBy(() -> runner.processTapCommand(tapCommand))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("러너와 탭 커맨드의 플레이어가 일치하지 않습니다.");
    }

    @Test
    void 러너가_현재_속도로_움직일_수_있다() {
        // given
        final Runner runner = new Runner(PlayerFixture.게스트한스());
        runner.adjustSpeed(5); // speed = 10

        // when
        runner.move();

        // then
        assertThat(runner.getPosition()).isEqualTo(10);
    }

    @Test
    void 러너가_완주_라인을_넘어도_정확히_1000에_위치한다() {
        // given
        final Runner runner = new Runner(PlayerFixture.게스트한스());
        runner.move(995); // position = 995

        // when
        runner.move(); // speed = 5, newPosition = 1000

        // then
        assertThat(runner.getPosition()).isEqualTo(1000);
    }

    @Test
    void 러너가_완주했는지_확인할_수_있다() {
        // given
        final Runner runner = new Runner(PlayerFixture.게스트한스());
        runner.move(1000);

        // when && then
        assertThat(runner.isFinished()).isTrue();
    }

    @Test
    void 러너가_완주하지_않았는지_확인할_수_있다() {
        // given
        final Runner runner = new Runner(PlayerFixture.게스트한스());
        runner.move(999);

        // when && then
        assertThat(runner.isFinished()).isFalse();
    }

    @Test
    void 러너가_이미_완주했는데_현재_속도로_움직이려하면_예외가_발생한다() {
        // given
        final Runner runner = new Runner(PlayerFixture.게스트한스());
        runner.move(1000);

        // when && then
        assertThatThrownBy(() -> runner.move())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 완주하여 더이상 움직일 수 없습니다.");
    }

}
