package coffeeshout.minigame.domain.cardgame;

import coffeeshout.minigame.domain.cardgame.card.AdditionCard;
import coffeeshout.minigame.domain.cardgame.card.Card;
import coffeeshout.minigame.domain.cardgame.card.MultiplierCard;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

class CardHandTest {

    @Test
    void 가지고_있는_카드들의_점수합을_반환한다() {
        // given
        CardHand cardHand = new CardHand();

        cardHand.put(new AdditionCard(10));
        cardHand.put(new MultiplierCard(-2));
        cardHand.put(new AdditionCard(30));

        // when
        CardGameScore cardGameScore = cardHand.calculateCardGameScore();

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(cardGameScore.getValue()).isEqualTo(-80);
            softly.assertThat(cardHand.size()).isEqualTo(3);
        });
    }

    @Test
    void 해당_라운드에_해당_카드를_뽑았으면_true를_반환한다() {
        // given
        CardHand cardHand = new CardHand();

        Card firstRoundCard = new AdditionCard(10);
        Card secondRoundCard = new MultiplierCard(-2);
        Card notPickedCard = new MultiplierCard(1);
        cardHand.put(firstRoundCard);
        cardHand.put(secondRoundCard);

        // when & then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(cardHand.isAssign(firstRoundCard, CardGameRound.FIRST)).isTrue();
            softly.assertThat(cardHand.isAssign(secondRoundCard, CardGameRound.SECOND)).isTrue();
            softly.assertThat(cardHand.isAssign(notPickedCard, CardGameRound.FIRST)).isFalse();
            softly.assertThat(cardHand.isAssign(notPickedCard, CardGameRound.SECOND)).isFalse();
        });
    }

    @Test
    void 해당_라운드에_카드를_뽑았으면_true를_반환한다() {
        // given
        CardHand cardHand = new CardHand();
        cardHand.put(new AdditionCard(10));

        // when & then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(cardHand.isSelected(CardGameRound.FIRST)).isTrue();
            softly.assertThat(cardHand.isSelected(CardGameRound.SECOND)).isFalse();
        });
    }
}
