package coffeeshout.coffeeshout.domain;

import static org.assertj.core.api.Assertions.assertThat;

import coffeeshout.coffeeshout.domain.player.Player;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MiniGameResultTest {

    private final Player player1 = new Player(null, "꾹", null, null);
    private final Player player2 = new Player(null, "한스", null, null);
    private final Player player3 = new Player(null, "민수", null, null);
    private final Player player4 = new Player(null, "지훈", null, null);

    @Test
    void 마지막_랭크를_탐색한다() {
        // given
        final MiniGameResult miniGameResult = new MiniGameResult(
                Map.of(1, player1, 2, player2, 3, player3, 4, player4)
        );

        // when
        int result = miniGameResult.getLastRank();

        // then
        assertThat(result).isEqualTo(4);
    }

    @Test
    void 중간_순위를_가져온다() {
        // given
        final MiniGameResult miniGameResult = new MiniGameResult(
                Map.of(1, player1, 2, player2, 3, player3, 4, player4)
        );

        // when
        double averageRank = miniGameResult.getCenterRank();

        // then
        assertThat(averageRank).isEqualTo(2.5);
    }
}
