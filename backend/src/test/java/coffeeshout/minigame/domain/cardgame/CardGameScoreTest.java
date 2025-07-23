package coffeeshout.minigame.domain.cardgame;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import coffeeshout.minigame.domain.cardgame.card.AdditionCard;
import coffeeshout.minigame.domain.cardgame.card.MultiplierCard;
import org.junit.jupiter.api.Test;

class CardGameScoreTest {

    @Test
    void 계산된_점수가_같으면_동일하다() {
        // given
        CardHand cardHand1 = new CardHand();
        cardHand1.put(new AdditionCard(40));
        cardHand1.put(new MultiplierCard(-1));
        CardGameScore score1 = new CardGameScore(cardHand1);

        CardHand cardHand2 = new CardHand();
        cardHand2.put(new AdditionCard(-10));
        cardHand2.put(new MultiplierCard(4));
        CardGameScore score2 = new CardGameScore(cardHand2);

        // when & then
        assertThat(score1.equals(score2)).isTrue();
    }

    @Test
    void 가지고_있는_카드들의_점수를_반환한다() {
        // given
        CardHand cardHand = new CardHand();

        cardHand.put(new AdditionCard(10));
        cardHand.put(new MultiplierCard(2));

        // when
        CardGameScore cardGameScore = new CardGameScore(cardHand);

        // then
        assertThat(cardGameScore.getResult()).isEqualTo(20);
    }
}
