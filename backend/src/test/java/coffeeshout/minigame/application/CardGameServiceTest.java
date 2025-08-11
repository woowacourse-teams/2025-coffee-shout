package coffeeshout.minigame.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import coffeeshout.config.TaskSchedulerConfig;
import coffeeshout.config.TestTaskScheduler;
import coffeeshout.fixture.MenuFixture;
import coffeeshout.fixture.PlayerProbabilitiesFixture;
import coffeeshout.global.ui.WebSocketResponse;
import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.CardGameTaskExecutorsV2;
import coffeeshout.minigame.domain.task.CardGameTaskType;
import coffeeshout.minigame.domain.task.MiniGameTaskManager;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@Import(TaskSchedulerConfig.class)
@TestPropertySource(properties = {
        "spring.main.allow-bean-definition-overriding=true"
})
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
        System.out.println("=== TEST SETUP ===");
        List<Player> players = PlayerProbabilitiesFixture.PLAYERS;
        host = players.get(0);
        Room room = roomService.createRoom(host.getName().value(), 1L);
        joinCode = room.getJoinCode();
        room.addMiniGame(host.getName(), MiniGameType.CARD_GAME.createMiniGame());

        for (int i = 1; i < players.size(); i++) {
            room.joinGuest(players.get(i).getName(), MenuFixture.아메리카노());
        }

        // 모든 플레이어가 준비 완료여야 한다.
        for (Player player : room.getPlayers()) {
            player.updateReadyState(true);
        }
    }
    @Autowired
    @Qualifier("miniGameTaskScheduler")
    TaskScheduler scheduler;

    @Test
    void 스케줄러_주입_확인() {
        System.out.println("📌 주입된 스케줄러 클래스: " + scheduler.getClass());
        assertThat(scheduler).isInstanceOf(TestTaskScheduler.class);
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
        void 카드게임이_종료되면_결과에_따라_룰렛의_가중치가_반영된다() throws InterruptedException, ExecutionException {
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
                    players.get(0), 1, // 꾹이 1등 / 가중치: -2500 * 0.7 = -1750 => 750
                    players.get(1), 2, // 루키 2등 / 가중치: -1250 * 0.7 = -875 => 1625
                    players.get(2), 3, // 엠제이 3등 / 가중치: +1250 * 0.7 = +875 => 3375
                    players.get(3), 4 // 한스 4등 / 가중치: +2500 * 0.7 = +4250
            ));

            doReturn(result).when(cardGameSpy).getResult();

            // when
            cardGameService.start(cardGameSpy, joinCode.value());
            cardGameTaskExecutors.get(joinCode).joinAll(CardGameTaskType.FIRST_ROUND_LOADING);

            Map<Player, Probability> probabilities = room.getProbabilities();
            assertThat(probabilities).containsExactlyInAnyOrderEntriesOf(Map.of(
                    players.get(0), new Probability(750),
                    players.get(1), new Probability(1625),
                    players.get(2), new Probability(3375),
                    players.get(3), new Probability(4250)));
        }

        @Test
        void 카드게임을_시작하면_태스크가_순차적으로_실행된다() throws InterruptedException, ExecutionException {
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

            cardGameTaskExecutors.get(joinCode).joinAll(CardGameTaskType.FIRST_ROUND_LOADING);

            // then
            verify(messagingTemplate, atLeast(6))
                    .convertAndSend(
                            eq("/topic/room/" + joinCode.getValue() + "/gameState"),
                            any(WebSocketResponse.class)
                    );
        }
    }

    @Nested
    class 카드_선택 {

        @Test
        void 카드를_정상적으로_선택한다() {
            // given
            Room room = roomQueryService.findByJoinCode(joinCode);
            CardGame cardGame = (CardGame) room.startNextGame(host.getName().value());
            cardGame.startPlay();

            // when
            cardGameService.selectCard(joinCode.getValue(), host.getName().value(), 0);

            // then
            assertThat(cardGame.getPlayerHands().findPlayerByName(host.getName())).isNotNull();
        }

        @Test
        void 카드_선택_후_게임_상태_메시지가_전송된다() {
            // given
            Room room = roomQueryService.findByJoinCode(joinCode);
            CardGame cardGame = (CardGame) room.startNextGame(host.getName().value());
            cardGame.startPlay();

            // when
            cardGameService.selectCard(joinCode.getValue(), host.getName().value(), 0);

            // then
            verify(messagingTemplate).convertAndSend(
                    eq("/topic/room/" + joinCode.getValue() + "/gameState"),
                    any(WebSocketResponse.class)
            );
        }

        @Test
        void 만약_선택된_카드를_고르면_예외를_반환한다() {
            // given
            Room room = roomQueryService.findByJoinCode(joinCode);
            CardGame cardGame = (CardGame) room.startNextGame(host.getName().value());
            cardGame.startPlay();
            List<Player> players = room.getPlayers();

            // when & then
            // 첫 번째 플레이어가 카드 선택
            cardGameService.selectCard(joinCode.getValue(), players.get(0).getName().value(), 0);

            // 두 번째 플레이어가 같은 카드 선택 시도 - 예외 발생해야 함
            assertThatThrownBy(() ->
                    cardGameService.selectCard(joinCode.getValue(), players.get(1).getName().value(), 0)
            ).isInstanceOf(IllegalStateException.class);
        }

        @Test
        void 게임이_플레이_상태가_아니면_예외를_반환한다() {
            // given
            Room room = roomQueryService.findByJoinCode(joinCode);
            room.startNextGame(host.getName().value());
            // PLAYING 상태로 변경하지 않음

            // when & then
            assertThatThrownBy(() ->
                    cardGameService.selectCard(joinCode.getValue(), host.getName().value(), 0)
            ).isInstanceOf(IllegalStateException.class);
        }

        @Test
        void 존재하지_않는_플레이어면_예외를_반환한다() {
            // given
            Room room = roomQueryService.findByJoinCode(joinCode);
            CardGame cardGame = (CardGame) room.startNextGame(host.getName().value());
            cardGame.startPlay();

            // when & then
            assertThatThrownBy(() ->
                    cardGameService.selectCard(joinCode.getValue(), "존재하지않는플레이어", 0)
            ).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void 잘못된_카드_인덱스면_예외를_반환한다() {
            // given
            Room room = roomQueryService.findByJoinCode(joinCode);
            CardGame cardGame = (CardGame) room.startNextGame(host.getName().value());
            cardGame.startPlay();

            // when & then
            assertThatThrownBy(() ->
                    cardGameService.selectCard(joinCode.getValue(), host.getName().value(), 999)
            ).isInstanceOf(IndexOutOfBoundsException.class);
        }

        @Test
        @Disabled
        void 라운드가_완료되면_플레이_태스크가_취소된다() throws InterruptedException, ExecutionException {
            // given
            Room room = roomQueryService.findByJoinCode(joinCode);
            CardGame cardGame = (CardGame) room.startNextGame(host.getName().value());
            cardGameService.start(cardGame, joinCode.getValue());
            cardGame.startPlay();
            List<Player> players = room.getPlayers();

            // when
            for (int i = 0; i < players.size(); i++) {
                cardGameService.selectCard(joinCode.getValue(), players.get(i).getName().value(), i);
            }

            Thread.sleep(5000);

            // then
            assertThat(cardGame.isFinishedThisRound()).isTrue();
        }
    }
}
