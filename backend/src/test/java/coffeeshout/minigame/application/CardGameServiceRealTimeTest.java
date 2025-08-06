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
import coffeeshout.minigame.domain.round.RoundPhase;
import coffeeshout.room.application.RoomService;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.service.RoomQueryService;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
        MockitoAnnotations.openMocks(this);

        // 모든 플레이어가 준비 완료여야 한다.
        for (Player player : room.getPlayers()) {
            player.updateReadyState(true);
        }
    }

//    @Disabled
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

            });
        }

        @Test
        void 카드게임을_시작하면_태스크가_순차적으로_실행된다() throws InterruptedException {
            Room room = roomQueryService.findByJoinCode(joinCode);
            CardGame cardGame = (CardGame) room.startNextGame(host.getName().value());
            cardGameService.start(cardGame, joinCode.value());

            /*
                READY       PLAYING         SCORE_BOARD      LOADING        PLAYING         SCORE_BOARD     DONE
                0~3000      3000~13000      13000~14500      14500~17500    17500~27500     27500~29000     29000~
             */
            Thread.sleep(1000);
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(cardGame.getCurrentPhase()).isEqualTo(RoundPhase.LOADING);
            });

            Thread.sleep(4000); // 총 5초 (LOADING 3초 + PLAYING 시작 후 1초)
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(cardGame.getPlayerHands().totalHandSize()).isEqualTo(0);
                softly.assertThat(cardGame.getCurrentPhase()).isEqualTo(RoundPhase.PLAYING);
            });

            Thread.sleep(8500);
            // 총 13.5초 지남 (LOADING 3초 + PLAYING 10초 + SCORE_BOARD 시작 후 0.5초)
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(cardGame.getPlayerHands().totalHandSize()).isEqualTo(4);
                softly.assertThat(cardGame.getCurrentPhase()).isEqualTo(RoundPhase.SCORING);
            });

            Thread.sleep(2500);
            // 총 16.0초 지남 (+ 1.5초 SCORE_BOARD + 0.5초 LOADING)
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(cardGame.getCurrentPhase()).isEqualTo(RoundPhase.LOADING);
            });

            Thread.sleep(3500);
            // 총 19초 지남 (+ 3초 LOADING + 0.5초 PLAYING)
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(cardGame.getPlayerHands().totalHandSize()).isEqualTo(4);
                softly.assertThat(cardGame.getCurrentPhase()).isEqualTo(RoundPhase.PLAYING);
            });

            Thread.sleep(9000);
            // 총 28초 지남 (+ 10초 PLAYING)
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(cardGame.getPlayerHands().totalHandSize()).isEqualTo(8);
                softly.assertThat(cardGame.getCurrentPhase()).isEqualTo(RoundPhase.SCORING);
            });

            Thread.sleep(2000);
            // 총 31초 지남 (+ 1.5초 SCORE_BOARD 완료 후)
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(cardGame.getCurrentPhase()).isEqualTo(RoundPhase.DONE);
            });
            verify(messagingTemplate, atLeast(6))
                    .convertAndSend(
                            eq("/topic/room/" + joinCode.getValue() + "/gameState"),
                            any(WebSocketResponse.class)
                    );
        }
    }
}
