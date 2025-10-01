package coffeeshout.minigame.cardgame.domain.cardgame;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import coffeeshout.minigame.cardgame.domain.cardgame.CardGameScore;
import coffeeshout.minigame.cardgame.domain.cardgame.CardHand;
import coffeeshout.minigame.cardgame.domain.cardgame.card.AdditionCard;
import coffeeshout.minigame.cardgame.domain.cardgame.card.MultiplierCard;
import org.junit.jupiter.api.Test;

class CardGameScoreTest {

    @Test
    void 카드_핸드로_점수를_생성한다() {
        // given
        CardHand hand = new CardHand();
        hand.put(AdditionCard.PLUS_30);
        hand.put(MultiplierCard.DOUBLE);

        // when
        CardGameScore score = new CardGameScore(hand);

        // then
        assertThat(score.getValue()).isEqualTo(60);
    }

    @Test
    void 덧셈_카드로_점수를_업데이트한다() {
        // given
        CardHand hand = new CardHand();
        hand.put(AdditionCard.PLUS_40);

        // when
        CardGameScore score = hand.calculateCardGameScore();

        // then
        assertThat(score.getValue()).isEqualTo(40);
    }

    @Test
    void 곱셈_카드로_점수를_업데이트한다() {
        // given
        CardHand hand = new CardHand();
        hand.put(AdditionCard.PLUS_10);
        hand.put(MultiplierCard.DOUBLE);

        // when
        CardGameScore score = hand.calculateCardGameScore();

        // then
        assertThat(score.getValue()).isEqualTo(20);
    }

    @Test
    void 음수_덧셈_카드로_점수를_업데이트한다() {
        // given
        CardHand hand = new CardHand();
        hand.put(AdditionCard.PLUS_30);
        hand.put(AdditionCard.MINUS_10);

        // when
        CardGameScore score = hand.calculateCardGameScore();

        // then
        assertThat(score.getValue()).isEqualTo(20);
    }

    @Test
    void 계산된_점수가_같으면_동일하다() {
        // given
        CardHand cardHand1 = new CardHand();
        cardHand1.put(AdditionCard.PLUS_40);
        cardHand1.put(MultiplierCard.INVERT);
        CardGameScore score1 = new CardGameScore(cardHand1);

        CardHand cardHand2 = new CardHand();
        cardHand2.put(AdditionCard.MINUS_10);
        cardHand2.put(MultiplierCard.QUADRUPLE);
        CardGameScore score2 = new CardGameScore(cardHand2);

        // when & then
        assertThat(score1.equals(score2)).isTrue();
    }
}
