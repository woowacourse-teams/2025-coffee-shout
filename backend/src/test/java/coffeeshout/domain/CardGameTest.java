package coffeeshout.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class CardGameTest {

    @Test
    void 카드를_선택한다() {
        // given
        final List<Player> players = PlayerFixture.players;
        final Player player1 = players.get(0);
        final Player player2 = players.get(1);
        final CardGame cardGame = new CardGame(players);

        // when
        cardGame.selectCard(player1, 0);

        // then
        assertThat(cardGame.getPlayerCards().get(player1)).hasSize(1);
        assertThat(cardGame.getPlayerCards().get(player2)).hasSize(0);
    }

}
