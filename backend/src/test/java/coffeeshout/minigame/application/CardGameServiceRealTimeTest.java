package coffeeshout.minigame.application;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

import coffeeshout.fixture.MenuFixture;
import coffeeshout.fixture.PlayerProbabilitiesFixture;
import coffeeshout.global.ui.WebSocketResponse;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.CardGameState;
import coffeeshout.minigame.domain.cardgame.CardGameTaskExecutorsV2;
import coffeeshout.minigame.domain.task.CardGameTaskType;
import coffeeshout.minigame.domain.task.MiniGameTaskManager;
import coffeeshout.room.application.RoomService;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.service.RoomQueryService;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class CardGameServiceRealTimeTest {

    @MockitoBean
    SimpMessagingTemplate messagingTemplate;

    @Autowired
    CardGameService cardGameService;

    @Autowired
    RoomQueryService roomQueryService;

    @Autowired
    RoomService roomService;

    @Autowired
    CardGameTaskExecutorsV2 cardGameTaskExecutors;

    JoinCode joinCode;

    Player host;

    @BeforeEach
    void setUp() {
        List<Player> players = PlayerProbabilitiesFixture.PLAYERS;
        host = players.get(0);
        Room room = roomService.createRoom(host.getName().value(), 1L);
        joinCode = room.getJoinCode();
        room.addMiniGame(host.getName(), MiniGameType.CARD_GAME.createMiniGame());

        for (int i = 1; i < players.size(); i++) {
            room.joinGuest(players.get(i).getName(), MenuFixture.아메리카노());
        }

        for (Player player : room.getPlayers()) {
            player.updateReadyState(true);
        }

        MockitoAnnotations.openMocks(this);
    }

    @Nested
    class 카드게임_시작 {

        @Test
        void 카드게임을_시작한다() {
            // given
            Room room = roomQueryService.findByJoinCode(joinCode);
            CardGame cardGame = (CardGame) room.startNextGame(host.getName().value());
            cardGameService.start(cardGame, joinCode.value());

            // when & then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(cardGame).isNotNull();
                softly.assertThat(cardGame.getDeck().size()).isEqualTo(9);
                softly.assertThat(cardGame.getPlayerHands().playerCount()).isEqualTo(4);

                MiniGameTaskManager<CardGameTaskType> executor = cardGameTaskExecutors.get(joinCode);
                softly.assertThat(executor).isNotNull();
            });
        }

        @Test
        void 카드게임을_시작하면_태스크가_순차적으로_실행된다() throws InterruptedException {
            Room room = roomQueryService.findByJoinCode(joinCode);
            CardGame cardGame = (CardGame) room.startNextGame(host.getName().value());
            cardGameService.start(cardGame, joinCode.value());

            /*
                READY       DESCRIPTION       PLAYING         SCORE_BOARD      LOADING        PLAYING         SCORE_BOARD     DONE
                0~3000      3000 ~ 4500      4500~14500      14500~16000      16000~18000    18000~28000     28000~29500     29500~
             */
            Thread.sleep(1000); // 1000
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(cardGame.getState()).isEqualTo(CardGameState.LOADING);
            });


            Thread.sleep(3000); // 4000
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(cardGame.getState()).isEqualTo(CardGameState.DESCRIPTION);
            });

            Thread.sleep(4500); // 8500
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(cardGame.getPlayerHands().totalHandSize()).isEqualTo(0);
                softly.assertThat(cardGame.getState()).isEqualTo(CardGameState.PLAYING);
            });

            Thread.sleep(6500); // 15000
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(cardGame.getPlayerHands().totalHandSize()).isEqualTo(4);
                softly.assertThat(cardGame.getState()).isEqualTo(CardGameState.SCORE_BOARD);
            });

            Thread.sleep(2500); // 17500
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(cardGame.getState()).isEqualTo(CardGameState.LOADING);
            });

            Thread.sleep(3500); // 21000
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(cardGame.getPlayerHands().totalHandSize()).isEqualTo(4);
                softly.assertThat(cardGame.getState()).isEqualTo(CardGameState.PLAYING);
            });

            Thread.sleep(9500); // 29500
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(cardGame.getPlayerHands().totalHandSize()).isEqualTo(8);
                softly.assertThat(cardGame.getState()).isEqualTo(CardGameState.SCORE_BOARD);
            });

            Thread.sleep(2000); // 31000
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(cardGame.getState()).isEqualTo(CardGameState.DONE);
            });
            verify(messagingTemplate, atLeast(6))
                    .convertAndSend(
                            eq("/topic/room/" + joinCode.getValue() + "/gameState"),
                            any(WebSocketResponse.class)
                    );
        }
    }
}
