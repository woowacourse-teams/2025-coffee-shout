package coffeeshout.coffeeshout.domain.roulette;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import coffeeshout.coffeeshout.domain.Menu;
import coffeeshout.coffeeshout.domain.MiniGameResult;
import coffeeshout.coffeeshout.domain.player.Player;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

class RouletteTest {

    private final Menu menu = new Menu();
    private final Player player1 = new Player(1L, "꾹", menu, null);
    private final Player player2 = new Player(2L, "한스", menu, null);
    private final Player player3 = new Player(3L, "민수", menu, null);
    private final Player player4 = new Player(4L, "지훈", menu, null);

    @Test
    void 플레이어_인원_수는_2명_이상이여야_한다() {
        // given
        final List<Player> players = List.of(player1);
        final int round = 1;

        assertThatThrownBy(() -> new Roulette(players, round, new JavaRandomGenerator()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 라운드_수는_양수여야_한다() {
        final List<Player> players = List.of(player1, player2);
        final int round = 0;

        assertThatThrownBy(() -> new Roulette(players, round, new JavaRandomGenerator()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 미니게임_결과에_따라_확률이_조정된다() {
        // given
        final List<Player> players = List.of(player1, player2, player3, player4);
        final int round = 3;
        final Roulette roulette = new Roulette(players, round, new JavaRandomGenerator());

        final MiniGameResult miniGameResult = new MiniGameResult(
                Map.of(player1, 1, player2, 2, player3, 3, player4, 4)
        );

        // when
        roulette.adjustProbability(miniGameResult);

        // when & then
        final Map<Player, Double> probabilities = roulette.getPlayerProbabilities();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(probabilities).containsEntry(player1, 17.0);
            softly.assertThat(probabilities).containsEntry(player2, 21.0);
            softly.assertThat(probabilities).containsEntry(player3, 29.0);
            softly.assertThat(probabilities).containsEntry(player4, 33.0);
        });
    }

    @Test
    void 당첨자를_뽑는다() {
        // given
        final List<Player> players = List.of(player1, player2, player3, player4);
        final int round = 1;
        final Roulette roulette = new Roulette(players, round, new FixedRandomGenerator());

        // when
        Player result = roulette.spin();

        assertThat(result).isEqualTo(player4);
    }
}