package coffeeshout.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

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

    @Test
    void 라운드를_증가시킨다() {
        // given
        final List<Player> players = PlayerFixture.players;
        final CardGame cardGame = new CardGame(players);

        // when
        assertThat(cardGame.getRound()).isEqualTo(CardGameRound.ONE);
        cardGame.nextRound();

        // then
        assertThat(cardGame.getRound()).isEqualTo(CardGameRound.TWO);
    }

    @Test
    void 카드를_섞는다() {
        // given
        final List<Player> players = PlayerFixture.players;
        final CardGame cardGame = new CardGame(players);

        MockedStatic<CardGameDeck> mockedDeck = mockStatic(CardGameDeck.class);

        // when
        cardGame.shuffle();

        // then
        mockedDeck.verify(CardGameDeck::spreadCards, times(1));
    }

    @Test
    void 사용자들의_점수를_계산한다() {
        // given
        final List<Player> players = PlayerFixture.players;
        final CardGame cardGame = new CardGame(players);

        final List<Card> cards = cardGame.getCards();

        final Player player1 = players.get(0);
        final Player player2 = players.get(1);

        cardGame.selectCard(player1, 0);
        cardGame.selectCard(player1, 1);
        cardGame.selectCard(player2, 2);
        cardGame.selectCard(player2, 3);

        final CardGameScore score1 = new CardGameScore();
        score1.addCard(cards.get(0));
        score1.addCard(cards.get(1));

        final CardGameScore score2 = new CardGameScore();
        score2.addCard(cards.get(2));
        score2.addCard(cards.get(3));

        // when
        final Map<Player, CardGameScore> scores = cardGame.calculateScores();

        // then
        assertThat(scores.get(player1)).isEqualTo(score1);
        assertThat(scores.get(player2)).isEqualTo(score2);
    }

    @Test
    void 첫번째_라운드가_끝났는지_확인한다() {
        // given
        final List<Player> players = PlayerFixture.players;
        final CardGame cardGame = new CardGame(players);

        cardGame.selectCard(players.get(0), 0);
        cardGame.selectCard(players.get(1), 1);
        cardGame.selectCard(players.get(2), 2);
        assertThat(cardGame.isFirstRoundFinished()).isFalse();
        cardGame.selectCard(players.get(3), 3);

        // when & then
        assertThat(cardGame.isFirstRoundFinished()).isTrue();
    }

    @Test
    void 두번째_라운드가_끝났는지_확인한다() {
        // given
        final List<Player> players = PlayerFixture.players;
        final CardGame cardGame = new CardGame(players);

        cardGame.selectCard(players.get(0), 0);
        cardGame.selectCard(players.get(1), 1);
        cardGame.selectCard(players.get(2), 2);
        cardGame.selectCard(players.get(3), 3);
        cardGame.selectCard(players.get(0), 4);
        cardGame.selectCard(players.get(1), 5);
        cardGame.selectCard(players.get(2), 6);
        assertThat(cardGame.isSecondRoundFinished()).isFalse();
        cardGame.selectCard(players.get(3), 7);

        // when & then
        assertThat(cardGame.isSecondRoundFinished()).isTrue();
    }

}
