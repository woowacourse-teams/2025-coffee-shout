package coffeeshout.minigame.domain.cardgame;

import static org.assertj.core.api.Assertions.assertThat;

import coffeeshout.minigame.domain.cardgame.card.AdditionCard;
import coffeeshout.minigame.domain.cardgame.card.Card;
import coffeeshout.minigame.domain.cardgame.card.MultiplierCard;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CardGameScoreTest {

    @Test
    void 기본_생성자로_생성한다() {
        // when
        CardGameScore score = new CardGameScore();

        // then
        assertThat(score.getResult()).isEqualTo(0);
    }

    @Test
    void 값을_가진_생성자로_생성한다() {
        // given
        int addition = 50;

        // when
        CardGameScore score = new CardGameScore(addition);

        // then
        assertThat(score.getResult()).isEqualTo(50);
    }

    @Test
    void 카드_핸드로_점수를_생성한다() {
        // given
        CardHand hand = new CardHand();
        hand.put(AdditionCard.PLUS_30);
        hand.put(MultiplierCard.DOUBLE);

        // when
        CardGameScore score = new CardGameScore(hand);

        // then
        assertThat(score.getResult()).isEqualTo(60);
    }

    @Test
    void 덧셈_카드로_점수를_업데이트한다() {
        // given
        CardGameScore score = new CardGameScore();
        Card additionCard = AdditionCard.PLUS_40;

        // when
        score.updateScore(additionCard);

        // then
        assertThat(score.getResult()).isEqualTo(40);
    }

    @Test
    void 곱셈_카드로_점수를_업데이트한다() {
        // given
        CardGameScore score = new CardGameScore();
        score.updateScore(AdditionCard.PLUS_10);
        Card multiplierCard = MultiplierCard.DOUBLE;

        // when
        score.updateScore(multiplierCard);

        // then
        assertThat(score.getResult()).isEqualTo(20);
    }

    @Test
    void 음수_덧셈_카드로_점수를_업데이트한다() {
        // given
        CardGameScore score = new CardGameScore();
        score.updateScore(AdditionCard.PLUS_30);

        // when
        score.updateScore(AdditionCard.MINUS_10);

        // then
        assertThat(score.getResult()).isEqualTo(20);
    }

    @Test
    void 무효화_카드로_점수를_업데이트한다() {
        // given
        CardGameScore score = new CardGameScore();
        score.updateScore(AdditionCard.PLUS_40);

        // when
        score.updateScore(MultiplierCard.NULLIFY);

        // then
        assertThat(score.getResult()).isEqualTo(0);
    }
}
