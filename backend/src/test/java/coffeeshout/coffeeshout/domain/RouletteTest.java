package coffeeshout.coffeeshout.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import coffeeshout.coffeeshout.domain.player.Player;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RouletteTest {

    private final Menu menu = new Menu();
    private final Player player1 = new Player(null, "꾹", menu, null);
    private final Player player2 = new Player(null, "한스", menu, null);
    private final Player player3 = new Player(null, "민수", menu, null);
    private final Player player4 = new Player(null, "지훈", menu, null);

    @Test
    void 플레이어_인원_수는_2명_이상이여야_한다() {
        // given
        final List<Player> players = List.of(player1);
        final int round = 1;

        assertThatThrownBy(() -> new Roulette(players, round))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 라운드_수는_양수여야_한다() {
        final List<Player> players = List.of(player1, player2);
        final int round = 0;

        assertThatThrownBy(() -> new Roulette(players, round))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 미니게임_결과에_따라_확률이_조정된다() {
        // given
        final List<Player> players = List.of(player1, player2, player3, player4);
        final int round = 1;
        final Roulette roulette = new Roulette(players, round);

        final MiniGameResult miniGameResult = new MiniGameResult(
                Map.of(1, player1, 2, player2, 3, player3, 4, player4)
        );

        // when
        roulette.adjustWeight(miniGameResult);

        // when & then
        final Map<Player, Double> probabilities = roulette.getPlayerProbabilities();
        assertThat(probabilities.get(player1)).isEqualTo(17.0);
        assertThat(probabilities.get(player2)).isEqualTo(21.0);
        assertThat(probabilities.get(player3)).isEqualTo(29.0);
        assertThat(probabilities.get(player4)).isEqualTo(33.0);
    }


}