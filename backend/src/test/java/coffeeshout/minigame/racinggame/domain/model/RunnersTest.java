package coffeeshout.minigame.racinggame.domain.model;

import static org.assertj.core.api.Assertions.*;

import coffeeshout.fixture.PlayerFixture;
import coffeeshout.racinggame.domain.Runner;
import coffeeshout.racinggame.domain.Runners;
import coffeeshout.room.domain.player.Player;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RunnersTest {

    @Test
    void 플레이어_목록으로_러너들을_생성할_수_있다() {
        // given
        final List<Player> players = List.of(PlayerFixture.호스트한스(), PlayerFixture.게스트한스());

        // when
        final Runners runners = new Runners(players);

        // then
        assertThat(runners.getPositions()).hasSize(2);
    }

    @Test
    void 플레이어의_속도를_조정할_수_있다() throws InterruptedException {
        // given
        final Player player = PlayerFixture.게스트한스();
        final Runners runners = new Runners(List.of(player));

        Thread.sleep(1000); // 1초 대기

        // when
        runners.adjustSpeed(player, 7); // 1초에 7번 클릭

        // then
        final Runner runner = runners.stream()
                .filter(r -> r.getPlayer().equals(player))
                .findFirst()
                .orElseThrow();
        assertThat(runner.getSpeed()).isEqualTo(7);
    }

    @Test
    void 존재하지_않는_플레이어의_속도_조정시_예외가_발생한다() {
        // given
        final Player player1 = PlayerFixture.게스트한스();
        final Player player2 = PlayerFixture.게스트꾹이(); // 다른 이름의 플레이어
        final Runners runners = new Runners(List.of(player1));

        // when && then
        assertThatThrownBy(() -> runners.adjustSpeed(player2, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 플레이어의 러너를 찾을 수 없습니다.");
    }

    @Test
    void 모든_러너를_이동시킬_수_있다() {
        // given
        final Player player1 = PlayerFixture.호스트한스();
        final Player player2 = PlayerFixture.게스트한스();
        final Runners runners = new Runners(List.of(player1, player2));

        // when
        runners.moveAll();

        // then
        final Map<Runner, Integer> positions = runners.getPositions();
        assertThat(positions.values()).allMatch(position -> position == 1); // INITIAL_SPEED = 1
    }

    @Test
    void 우승자를_찾을_수_있다() throws InterruptedException {
        // given
        final Player player = PlayerFixture.게스트한스();
        final Runners runners = new Runners(List.of(player));

        Thread.sleep(100);
        runners.adjustSpeed(player, 10); // speed = 10
        for (int i = 0; i < 100; i++) {
            runners.moveAll();
        }

        // when && then
        final Runner winner = runners.findWinner().orElseThrow();
        assertThat(winner.getPlayer()).isEqualTo(player);
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
    void 우승자가_있는지_확인할_수_있다() throws InterruptedException {
        // given
        final Player player = PlayerFixture.게스트한스();
        final Runners runners = new Runners(List.of(player));

        Thread.sleep(100);
        runners.adjustSpeed(player, 10);
        for (int i = 0; i < 100; i++) {
            runners.moveAll();
        }

        // when && then
        assertThat(runners.hasWinner()).isTrue();
    }

    @Test
    void 모든_러너의_위치를_조회할_수_있다() {
        // given
        final Player player1 = PlayerFixture.호스트한스();
        final Player player2 = PlayerFixture.게스트한스();
        final Runners runners = new Runners(List.of(player1, player2));

        runners.moveAll();

        // when
        final Map<Runner, Integer> positions = runners.getPositions();

        // then
        assertThat(positions).hasSize(2);
        assertThat(positions.values()).allMatch(position -> position == 1); // INITIAL_SPEED = 1
    }

    @Test
    void 모든_러너의_속도를_조회할_수_있다() throws InterruptedException {
        // given
        final Player player1 = PlayerFixture.호스트한스();
        final Player player2 = PlayerFixture.게스트한스();
        final Runners runners = new Runners(List.of(player1, player2));

        Thread.sleep(1000);
        runners.adjustSpeed(player1, 8); // 1초에 8번 클릭

        // when
        final Map<Runner, Integer> speeds = runners.getSpeeds();

        // then
        assertThat(speeds).hasSize(2);
        boolean hasPlayer1WithSpeed8 = speeds.entrySet().stream()
                .anyMatch(entry -> entry.getKey().getPlayer().equals(player1) && entry.getValue() == 8);
        boolean hasPlayer2WithSpeed1 = speeds.entrySet().stream()
                .anyMatch(entry -> entry.getKey().getPlayer().equals(player2) && entry.getValue() == 1);

        assertThat(hasPlayer1WithSpeed8).isTrue();
        assertThat(hasPlayer2WithSpeed1).isTrue();
    }

    @Test
    void 모든_러너가_완주했는지_확인할_수_있다() throws InterruptedException {
        // given
        final Player player = PlayerFixture.게스트한스();
        final Runners runners = new Runners(List.of(player));

        Thread.sleep(100);
        runners.adjustSpeed(player, 10);

        // 완주시킴
        for (int i = 0; i < 100; i++) {
            runners.moveAll();
        }

        // when && then
        assertThat(runners.isAllFinished()).isTrue();
    }

    @Test
    void 모든_러너가_완주하지_않았는지_확인할_수_있다() {
        // given
        final Player player1 = PlayerFixture.호스트한스();
        final Player player2 = PlayerFixture.게스트한스();
        final Runners runners = new Runners(List.of(player1, player2));

        // when && then
        assertThat(runners.isAllFinished()).isFalse();
    }
}
