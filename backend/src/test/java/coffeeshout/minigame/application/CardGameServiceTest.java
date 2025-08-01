package coffeeshout.minigame.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import coffeeshout.fixture.MenuFixture;
import coffeeshout.fixture.PlayerProbabilities;
import coffeeshout.global.ui.WebSocketResponse;
import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.CardGameTaskExecutors;
import coffeeshout.minigame.domain.cardgame.CardGameTaskExecutorsV2;
import coffeeshout.minigame.domain.executor.CardGameTaskInfo;
import coffeeshout.minigame.domain.executor.TaskExecutor;
import coffeeshout.minigame.domain.temp.CardGameTaskType;
import coffeeshout.minigame.domain.temp.MiniGameTaskManager;
import coffeeshout.room.application.RoomService;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Playable;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.roulette.Probability;
import coffeeshout.room.domain.service.RoomQueryService;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@Import({TestConfig.class})
class CardGameServiceTest {

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
        List<Player> players = PlayerProbabilities.PLAYERS;
        host = players.get(0);
        Room room = roomService.createRoom(host.getName().value(), 1L);
        joinCode = room.getJoinCode();
        room.addMiniGame(host.getName(), MiniGameType.CARD_GAME.createMiniGame());

        for (int i = 1; i < players.size(); i++) {
            room.joinGuest(players.get(i).getName(), MenuFixture.아메리카노());
        }
    }

    @Nested
    class 카드게임_시작 {

        @Test
        void 카드게임을_시작한다() {
            // given
            Room room = roomQueryService.findByJoinCode(joinCode);
            Playable currentGame = room.startNextGame(host.getName().value());
            cardGameService.start(currentGame, joinCode.value());
            CardGame cardGame = (CardGame) room.findMiniGame(MiniGameType.CARD_GAME);

            // when & then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(cardGame).isNotNull();
                softly.assertThat(cardGame.getDeck().size()).isEqualTo(9);
                softly.assertThat(cardGame.getPlayerHands().playerCount()).isEqualTo(4);

                MiniGameTaskManager<CardGameTaskType> executor = cardGameTaskExecutors.get(joinCode);
                softly.assertThat(executor).isNotNull();

//                softly.assertThat(executor.getFutureTasks()).hasSize(7);
            });
        }

        @Test
        void 카드게임이_종료되면_결과에_따라_룰렛의_가중치가_반영된다() throws InterruptedException {
            // given
            CountDownLatch latch = new CountDownLatch(1); // 예상되는 메시지 수

            doAnswer(invocation -> {
                latch.countDown();
                return null;
            }).when(messagingTemplate).convertAndSend(
                    eq("/topic/room/" + joinCode.getValue() + "/rank"),
                    any(WebSocketResponse.class)
            );

            Room room = roomQueryService.findByJoinCode(joinCode);
            CardGame cardGame = (CardGame) room.startNextGame(host.getName().value());
            CardGame cardGameSpy = spy(cardGame);
            List<Player> players = room.getPlayers();
            MiniGameResult result = new MiniGameResult(Map.of(
                    players.get(0), 1, // 꾹이 1등 / 확률: 0
                    players.get(1), 2, // 루키 2등 / 확률: 1250
                    players.get(2), 3, // 엠제이 3등 / 확률: 3750
                    players.get(3), 4 // 한스 4등 / 확률: 5000
            ));

            doReturn(result).when(cardGameSpy).getResult();

            // when
            cardGameService.start(cardGameSpy, joinCode.value());
            assertThat(latch.await(1, TimeUnit.SECONDS)).isTrue();


            Map<Player, Probability> probabilities = room.getProbabilities();
            assertThat(probabilities).containsExactlyInAnyOrderEntriesOf(Map.of(
                    players.get(0), new Probability(0),
                    players.get(1), new Probability(1250),
                    players.get(2), new Probability(3750),
                    players.get(3), new Probability(5000)));
        }

        @Test
        void 카드게임_종료되면_결과에_따른_점수를_응답한다() throws InterruptedException {
            // given
            Room room = roomQueryService.findByJoinCode(joinCode);
            Playable playable = room.startNextGame(host.getName().value());
            CountDownLatch latch = new CountDownLatch(1); // 예상되는 메시지 수

            doAnswer(invocation -> {
                latch.countDown();
                return null;
            }).when(messagingTemplate).convertAndSend(
                    eq("/topic/room/" + joinCode.getValue() + "/rank"),
                    any(WebSocketResponse.class)
            );

            // when
            cardGameService.start(playable, joinCode.value());
            assertThat(latch.await(1, TimeUnit.SECONDS)).isTrue();

            // then
            verify(messagingTemplate, atLeast(1))
                    .convertAndSend(
                            eq("/topic/room/" + joinCode.getValue() + "/score"),
                            any(WebSocketResponse.class)
                    );

            verify(messagingTemplate, atLeast(1))
                    .convertAndSend(
                            eq("/topic/room/" + joinCode.getValue() + "/rank"),
                            any(WebSocketResponse.class)
                    );
        }

        @Test
        void 카드게임을_시작하면_태스크가_순차적으로_실행된다() throws InterruptedException {
            // given
            CountDownLatch latch = new CountDownLatch(6); // 예상되는 메시지 수

            doAnswer(invocation -> {
                latch.countDown();
                return null;
            }).when(messagingTemplate).convertAndSend(
                    eq("/topic/room/" + joinCode.getValue() + "/gameState"),
                    any(WebSocketResponse.class)
            );

            Room room = roomQueryService.findByJoinCode(joinCode);
            Playable miniGame = room.startNextGame(host.getName().value());
            // when
            cardGameService.start(miniGame, joinCode.value());

            assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();

            // then
            verify(messagingTemplate, atLeast(6))
                    .convertAndSend(
                            eq("/topic/room/" + joinCode.getValue() + "/gameState"),
                            any(WebSocketResponse.class)
                    );
        }
    }
}
