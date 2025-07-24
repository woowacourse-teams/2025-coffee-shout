package coffeeshout.minigame.application;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.CardGameQueryService;
import coffeeshout.minigame.domain.cardgame.CardGameState;
import coffeeshout.minigame.domain.cardgame.CardGameTaskExecutor;
import coffeeshout.minigame.domain.cardgame.CardGameTaskExecutors;
import coffeeshout.minigame.ui.MiniGameStateMessage;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@SpringBootTest
class CardGameServiceTest {

    @MockBean
    SimpMessagingTemplate messagingTemplate;

    @Autowired
    CardGameService cardGameService;

    @Autowired
    CardGameQueryService cardGameQueryService;

    @Autowired
    CardGameTaskExecutors cardGameTaskExecutors;

    Long roomId = 1L;

    @Nested
    class 카드게임_시작 {

        @Test
        void 카드게임을_시작한다() {
            // given
            cardGameService.startGame(roomId);

            // when & then
            SoftAssertions.assertSoftly(softly -> {

                CardGame cardGame = cardGameQueryService.getCardGame(roomId);

                softly.assertThat(cardGame).isNotNull();
                softly.assertThat(cardGame.getDeck().size()).isEqualTo(9);
                softly.assertThat(cardGame.getPlayerHands().playerCount()).isEqualTo(4);

                CardGameTaskExecutor executor = cardGameTaskExecutors.get(roomId);
                softly.assertThat(executor).isNotNull();

                softly.assertThat(executor.getCardGameTasks()).hasSize(6);
            });
        }

        @Test
        void 카드게임을_시작하면_태스크가_순차적으로_실행된다() throws InterruptedException {
            cardGameService.startGame(roomId);
            CardGame cardGame = cardGameQueryService.getCardGame(roomId);

            Thread.sleep(5000);
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(cardGame.getPlayerHands().totalHandSize()).isEqualTo(0);
                softly.assertThat(cardGame.getState()).isEqualTo(CardGameState.PLAYING);
            });

            Thread.sleep(5500);
            // 10.5초 지남
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(cardGame.getPlayerHands().totalHandSize()).isEqualTo(4);
                softly.assertThat(cardGame.getState()).isEqualTo(CardGameState.SCORE_BOARD);
            });

            Thread.sleep(1700);
            // 12.2초 지남
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(cardGame.getState()).isEqualTo(CardGameState.LOADING);
            });

            Thread.sleep(3300);
            // 15.5초 지남
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(cardGame.getPlayerHands().totalHandSize()).isEqualTo(4);
                softly.assertThat(cardGame.getState()).isEqualTo(CardGameState.PLAYING);
            });

            Thread.sleep(10000);
            // 25.5초 지남
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(cardGame.getPlayerHands().totalHandSize()).isEqualTo(8);
                softly.assertThat(cardGame.getState()).isEqualTo(CardGameState.SCORE_BOARD);
            });

            Thread.sleep(2000);
            // 27.5초 지남
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(cardGame.getState()).isEqualTo(CardGameState.DONE);
            });

            Thread.sleep(2000);
            verify(messagingTemplate, times(5))
                    .convertAndSend(eq("/topic/room/" + roomId + "/gameState"), any(MiniGameStateMessage.class));
        }
    }


}
