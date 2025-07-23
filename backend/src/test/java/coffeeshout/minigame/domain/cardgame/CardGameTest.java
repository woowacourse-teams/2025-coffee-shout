package coffeeshout.minigame.domain.cardgame;

import static org.assertj.core.api.Assertions.assertThat;

import coffeeshout.fixture.CardGameDeckStub;
import coffeeshout.fixture.CardGameFake;
import coffeeshout.fixture.PlayersFixture;
import coffeeshout.minigame.domain.cardgame.card.CardGameDeckGenerator;
import coffeeshout.player.domain.Player;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CardGameTest {

    CardGame cardGame;

    List<Player> players;

    CardGameDeckGenerator deckGenerator = new CardGameDeckStub();

    @BeforeEach
    void setUp() {
        players = PlayersFixture.꾹이_루키_엠제이_한스().getPlayers();
        cardGame = new CardGameFake(deckGenerator, players);

        cardGame.startRound();
    }

    @Test
    void 카드를_선택한다() {
        // given
        Player player1 = players.get(0);

        // when
        cardGame.selectCard(player1, 0);

        // then
        assertThat(cardGame.getPlayerHands().totalHandSize()).isEqualTo(1);
    }

    @Test
    void 라운드를_증가시킨다() {
        // given

        // when
        assertThat(cardGame.getRound()).isEqualTo(CardGameRound.FIRST);
        cardGame.startRound();

        // then
        assertThat(cardGame.getRound()).isEqualTo(CardGameRound.SECOND);
    }

    @Test
    void 사용자들의_점수를_계산한다() {
        // given
        Player player1 = players.get(0);
        Player player2 = players.get(1);

        cardGame.selectCard(player1, 0);
        cardGame.selectCard(player1, 1);
        cardGame.selectCard(player2, 2);
        cardGame.selectCard(player2, 3);

        // when
        Map<Player, CardGameScore> scores = cardGame.calculateScores();

        // then
        assertThat(scores.get(player1).getResult()).isEqualTo(70);
        assertThat(scores.get(player2).getResult()).isEqualTo(30);
    }

    @Test
    void 첫번째_라운드가_끝났는지_확인한다() {
        // given
        cardGame.selectCard(players.get(0), 0);
        cardGame.selectCard(players.get(1), 1);
        cardGame.selectCard(players.get(2), 2);
        assertThat(cardGame.isFinished(CardGameRound.FIRST)).isFalse();
        cardGame.selectCard(players.get(3), 3);

        // when & then
        assertThat(cardGame.isFinished(CardGameRound.FIRST)).isTrue();
    }

    @Test
    void 두번째_라운드가_끝났는지_확인한다() {
        // given
        cardGame.selectCard(players.get(0), 0);
        cardGame.selectCard(players.get(1), 1);
        cardGame.selectCard(players.get(2), 2);
        cardGame.selectCard(players.get(3), 3);
        cardGame.startRound();
        cardGame.selectCard(players.get(0), 4);
        cardGame.selectCard(players.get(1), 5);
        cardGame.selectCard(players.get(2), 6);
        assertThat(cardGame.isFinished(CardGameRound.SECOND)).isFalse();
        cardGame.selectCard(players.get(3), 7);

        // when & then
        assertThat(cardGame.isFinished(CardGameRound.SECOND)).isTrue();
    }
}
