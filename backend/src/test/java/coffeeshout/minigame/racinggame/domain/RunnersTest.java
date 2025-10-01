package coffeeshout.minigame.racinggame.domain;

import static org.assertj.core.api.Assertions.*;

import coffeeshout.fixture.PlayerFixture;
import coffeeshout.room.domain.player.Player;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RunnersTest {

    @Test
    void 플레이어_목록으로_러너들을_생성할_수_있다() {
        // given
        final List<Player> players = List.of(PlayerFixture.호스트피케이(), PlayerFixture.게스트한스());

        // when
        final Runners runners = new Runners(players);

        // then
        assertThat(runners.getPositions()).hasSize(2);
    }

    @Test
    void 플레이어의_속도를_조정할_수_있다() {
        // given
        final Player player = PlayerFixture.게스트한스();
        final Runners runners = new Runners(List.of(player));
        final Instant firstTime = Instant.now();
        final Instant secondTime = firstTime.plusMillis(1000);

        // when
        runners.adjustSpeed(player, 7, secondTime); // 1초에 7번 클릭

        // then
        assertThat(runners.getSpeeds().get(player)).isEqualTo(7);
    }

    @Test
    void 존재하지_않는_플레이어의_속도_조정시_예외가_발생한다() {
        // given
        final Player player = PlayerFixture.게스트한스();
        final Runners runners = new Runners(List.of(player));
        final Instant timestamp = Instant.now();

        // when && then
        assertThatThrownBy(() -> runners.adjustSpeed(PlayerFixture.호스트피케이(), 10, timestamp))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 플레이어의 러너를 찾을 수 없습니다.");
    }

    @Test
    void 모든_러너를_이동시킬_수_있다() {
        // given
        final Player player1 = PlayerFixture.호스트피케이();
        final Player player2 = PlayerFixture.게스트한스();
        final Runners runners = new Runners(List.of(player1, player2));

        // when
        runners.moveAll();

        // then
        assertThat(runners.getPositions().get(player1)).isEqualTo(5);
        assertThat(runners.getPositions().get(player2)).isEqualTo(5);
    }

    @Test
    void 우승자를_찾을_수_있다() {
        // given
        final Player player = PlayerFixture.게스트한스();
        final Runners runners = new Runners(List.of(player));
        final Instant firstTime = Instant.now();
        final Instant secondTime = firstTime.plusMillis(100);

        runners.adjustSpeed(player, 10, secondTime); // speed = 10
        for (int i = 0; i < 100; i++) {
            runners.moveAll();
        }

        // when && then
        assertThat(runners.findWinner()).contains(player);
    }

    @Test
    void 우승자가_없으면_빈_Optional을_반환한다() {
        // given
        final Player player = PlayerFixture.게스트한스();
        final Runners runners = new Runners(List.of(player));

        // when && then
        assertThat(runners.findWinner()).isEmpty();
    }

    @Test
    void 우승자가_있는지_확인할_수_있다() {
        // given
        final Player player = PlayerFixture.게스트한스();
        final Runners runners = new Runners(List.of(player));
        final Instant firstTime = Instant.now();
        final Instant secondTime = firstTime.plusMillis(100);

        runners.adjustSpeed(player, 10, secondTime);
        for (int i = 0; i < 100; i++) {
            runners.moveAll();
        }

        // when && then
        assertThat(runners.hasWinner()).isTrue();
    }

    @Test
    void 모든_러너의_위치를_조회할_수_있다() {
        // given
        final Player player1 = PlayerFixture.호스트피케이();
        final Player player2 = PlayerFixture.게스트한스();
        final Runners runners = new Runners(List.of(player1, player2));

        runners.moveAll();

        // when
        final Map<Player, Integer> positions = runners.getPositions();

        // then
        assertThat(positions).hasSize(2);
        assertThat(positions.get(player1)).isEqualTo(5);
        assertThat(positions.get(player2)).isEqualTo(5);
    }

    @Test
    void 모든_러너의_속도를_조회할_수_있다() {
        // given
        final Player player1 = PlayerFixture.호스트피케이();
        final Player player2 = PlayerFixture.게스트한스();
        final Runners runners = new Runners(List.of(player1, player2));
        final Instant firstTime = Instant.now();
        final Instant secondTime = firstTime.plusMillis(1000);

        runners.adjustSpeed(player1, 8, secondTime); // 1초에 8번 클릭

        // when
        final Map<Player, Integer> speeds = runners.getSpeeds();

        // then
        assertThat(speeds).hasSize(2);
        assertThat(speeds.get(player1)).isEqualTo(8);
        assertThat(speeds.get(player2)).isEqualTo(5);
    }

    @Test
    void 완주_순서대로_순위를_조회할_수_있다() throws InterruptedException {
        // given
        final Player player1 = PlayerFixture.호스트피케이();
        final Player player2 = PlayerFixture.게스트한스();
        final Runners runners = new Runners(List.of(player1, player2));
        final Instant firstTime = Instant.now();
        final Instant secondTime = firstTime.plusMillis(100);

        runners.adjustSpeed(player1, 10, secondTime); // player1 속도 10
        runners.adjustSpeed(player2, 10, secondTime); // player2 속도 10

        // player1을 먼저 완주시킴
        for (int i = 0; i < 100; i++) {
            runners.moveAll();
        }
        Thread.sleep(10); // 시간 차이 발생
        for (int i = 0; i < 100; i++) {
            runners.moveAll();
        }

        // when
        final List<Player> ranking = runners.getRanking();

        // then
        assertThat(ranking).hasSize(2);
        assertThat(ranking.get(0)).isEqualTo(player1);
        assertThat(ranking.get(1)).isEqualTo(player2);
    }

    @Test
    void 모든_러너가_완주했는지_확인할_수_있다() {
        // given
        final Player player1 = PlayerFixture.호스트피케이();
        final Player player2 = PlayerFixture.게스트한스();
        final Runners runners = new Runners(List.of(player1, player2));
        final Instant firstTime = Instant.now();
        final Instant secondTime = firstTime.plusMillis(100);

        runners.adjustSpeed(player1, 10, secondTime);
        runners.adjustSpeed(player2, 10, secondTime);

        for (int i = 0; i < 100; i++) {
            runners.moveAll();
        }

        // when && then
        assertThat(runners.isAllFinished()).isTrue();
    }

    @Test
    void 모든_러너가_완주하지_않았는지_확인할_수_있다() {
        // given
        final Player player1 = PlayerFixture.호스트피케이();
        final Player player2 = PlayerFixture.게스트한스();
        final Runners runners = new Runners(List.of(player1, player2));

        // when && then
        assertThat(runners.isAllFinished()).isFalse();
    }
}
