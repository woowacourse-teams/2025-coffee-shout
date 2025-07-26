package coffeeshout.minigame.application;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.CardGameQueryService;
import coffeeshout.minigame.domain.cardgame.CardGameState;
import coffeeshout.minigame.domain.cardgame.CardGameTaskExecutors;
import coffeeshout.minigame.ui.MiniGameStateMessage;
import coffeeshout.room.domain.JoinCode;
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

    JoinCode joinCode = new JoinCode("ABCDE");

    @Nested
    class 카드게임_시작 {

        @Test
        void 카드게임을_시작한다() {
            // given
            cardGameService.startGame(joinCode);

            // when & then
            SoftAssertions.assertSoftly(softly -> {

                CardGame cardGame = cardGameQueryService.getCardGame(joinCode);

                softly.assertThat(cardGame).isNotNull();
                softly.assertThat(cardGame.getDeck().size()).isEqualTo(9);
                softly.assertThat(cardGame.getPlayerHands().playerCount()).isEqualTo(4);

                CardGameTaskExecutor executor = cardGameTaskExecutors.get(joinCode);
                softly.assertThat(executor).isNotNull();

                softly.assertThat(executor.getFutureTasks()).hasSize(7);
            });
        }

        @Test
        void 카드게임을_시작하면_태스크가_순차적으로_실행된다() throws InterruptedException {
            cardGameService.startGame(joinCode);
            CardGame cardGame = cardGameQueryService.getCardGame(joinCode);

            /*
                READY       PLAYING         SCORE_BOARD      LOADING        PLAYING         SCORE_BOARD     DONE
                0~3000      3000~13000      13000~14500      14500~17500    17500~27500     27500~29000     29000~
             */

            Thread.sleep(1000);
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(cardGame.getState()).isEqualTo(CardGameState.LOADING);
            });

            Thread.sleep(4000); // 총 5초 (LOADING 3초 + PLAYING 시작 후 1초)
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(cardGame.getPlayerHands().totalHandSize()).isEqualTo(0);
                softly.assertThat(cardGame.getState()).isEqualTo(CardGameState.PLAYING);
            });

            Thread.sleep(8500);
            // 총 13.5초 지남 (LOADING 3초 + PLAYING 10초 + SCORE_BOARD 시작 후 0.5초)
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(cardGame.getPlayerHands().totalHandSize()).isEqualTo(4);
                softly.assertThat(cardGame.getState()).isEqualTo(CardGameState.SCORE_BOARD);
            });

            Thread.sleep(2500);
            // 총 16.0초 지남 (+ 1.5초 SCORE_BOARD + 0.5초 LOADING)
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(cardGame.getState()).isEqualTo(CardGameState.LOADING);
            });

            Thread.sleep(3500);
            // 총 19초 지남 (+ 3초 LOADING + 0.5초 PLAYING)
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(cardGame.getPlayerHands().totalHandSize()).isEqualTo(4);
                softly.assertThat(cardGame.getState()).isEqualTo(CardGameState.PLAYING);
            });

            Thread.sleep(9000);
            // 총 28초 지남 (+ 10초 PLAYING)
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(cardGame.getPlayerHands().totalHandSize()).isEqualTo(8);
                softly.assertThat(cardGame.getState()).isEqualTo(CardGameState.SCORE_BOARD);
            });

            Thread.sleep(2000);
            // 총 31초 지남 (+ 1.5초 SCORE_BOARD 완료 후)
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(cardGame.getState()).isEqualTo(CardGameState.DONE);
            });
            verify(messagingTemplate, atLeast(6))
                    .convertAndSend(eq("/topic/room/" + joinCode.getValue() + "/gameState"), any(MiniGameStateMessage.class));
        }
    }


}
