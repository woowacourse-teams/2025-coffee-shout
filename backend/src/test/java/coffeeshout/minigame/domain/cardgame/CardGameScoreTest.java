package coffeeshout.minigame.domain.cardgame;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import coffeeshout.minigame.domain.cardgame.card.Card;
import coffeeshout.minigame.domain.cardgame.card.CardType;
import org.junit.jupiter.api.Test;

class CardGameScoreTest {

    @Test
    void 카드_핸드로_점수를_생성한다() {
        // given
        CardHand hand = new CardHand();
        hand.put(new Card(CardType.ADDITION, 30));
        hand.put(new Card(CardType.MULTIPLIER, 2));

        // when
        CardGameScore score = new CardGameScore(hand);

        // then
        assertThat(score.getValue()).isEqualTo(60);
    }

    @Test
    void 덧셈_카드로_점수를_업데이트한다() {
        // given
        CardHand hand = new CardHand();
        hand.put(new Card(CardType.ADDITION, 40));

        // when
        CardGameScore score = hand.calculateCardGameScore();

        // then
        assertThat(score.getValue()).isEqualTo(40);
    }

    @Test
    void 곱셈_카드로_점수를_업데이트한다() {
        // given
        CardHand hand = new CardHand();
        hand.put(new Card(CardType.ADDITION, 10));
        hand.put(new Card(CardType.MULTIPLIER, 2));

        // when
        CardGameScore score = hand.calculateCardGameScore();

        // then
        assertThat(score.getValue()).isEqualTo(20);
    }

    @Test
    void 음수_덧셈_카드로_점수를_업데이트한다() {
        // given
        CardHand hand = new CardHand();
        hand.put(new Card(CardType.ADDITION, 30));
        hand.put(new Card(CardType.ADDITION, -10));

        // when
        CardGameScore score = hand.calculateCardGameScore();

        // then
        assertThat(score.getValue()).isEqualTo(20);
    }

    @Test
    void 계산된_점수가_같으면_동일하다() {
        // given
        CardHand cardHand1 = new CardHand();
        cardHand1.put(new Card(CardType.ADDITION, 40));
        cardHand1.put(new Card(CardType.MULTIPLIER, -1));
        CardGameScore score1 = new CardGameScore(cardHand1);

        CardHand cardHand2 = new CardHand();
        cardHand2.put(new Card(CardType.ADDITION, -10));
        cardHand2.put(new Card(CardType.MULTIPLIER, 4));
        CardGameScore score2 = new CardGameScore(cardHand2);

        // when & then
        assertThat(score1.equals(score2)).isTrue();
    }
}
