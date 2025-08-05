package coffeeshout.minigame.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

import coffeeshout.fixture.MenuFixture;
import coffeeshout.fixture.PlayerProbabilitiesFixture;
import coffeeshout.global.ui.WebSocketResponse;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.round.RoundManagerRegistry;
import coffeeshout.minigame.domain.cardgame.round.RoundPhase;
import coffeeshout.room.application.RoomService;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Playable;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.service.RoomQueryService;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
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
    RoundManagerRegistry roundManagerRegistry;

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

        // 모든 플레이어가 준비 완료여야 한다.
        for (Player player : room.getPlayers()) {
            player.updateReadyState(true);
        }
    }

    @Nested
    class 카드게임_시작 {

        @Test
        void 카드게임을_시작한다() {
            // given
            Room room = roomQueryService.findByJoinCode(joinCode);
            Playable currentGame = room.startNextGame(host.getName().value());
            
            // when
            cardGameService.start(currentGame, joinCode.value());
            
            // then
            CardGame cardGame = (CardGame) room.findMiniGame(MiniGameType.CARD_GAME);
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(cardGame).isNotNull();
                softly.assertThat(cardGame.getDeck().size()).isEqualTo(9);
                softly.assertThat(cardGame.getPlayerHands().playerCount()).isEqualTo(4);
                softly.assertThat(cardGame.getRoundState()).isNotNull();
                softly.assertThat(cardGame.getRoundState().getPhase()).isNotEqualTo(RoundPhase.READY);
                
                // RoundManager가 등록되었는지 확인
                softly.assertThat(roundManagerRegistry.exists(joinCode)).isTrue();
            });
        }

        @Test
        void 카드게임을_시작하면_RoundManager가_등록된다() {
            // given
            Room room = roomQueryService.findByJoinCode(joinCode);
            Playable miniGame = room.startNextGame(host.getName().value());

            // when
            cardGameService.start(miniGame, joinCode.value());

            // then
            assertThat(roundManagerRegistry.exists(joinCode)).isTrue();
            assertThat(roundManagerRegistry.getActiveRoomCount()).isGreaterThan(0);
        }

        @Test
        void 방_정리_시_RoundManager도_함께_정리된다() {
            // given
            Room room = roomQueryService.findByJoinCode(joinCode);
            Playable miniGame = room.startNextGame(host.getName().value());
            cardGameService.start(miniGame, joinCode.value());

            assertThat(roundManagerRegistry.exists(joinCode)).isTrue();

            // when
            cardGameService.cleanupRoom(joinCode.value());

            // then
            assertThat(roundManagerRegistry.exists(joinCode)).isFalse();
        }
        
        @Test
        void 카드게임을_시작하면_WebSocket_메시지가_전송된다() throws InterruptedException {
            // given
            CountDownLatch latch = new CountDownLatch(1);

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

            // then
            verify(messagingTemplate, atLeast(1))
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
            cardGameService.start(cardGame, joinCode.value());

            // PLAYING 상태로 수동 변경 (테스트용)
            cardGame.setRoundState(cardGame.getRoundState().nextPhase(cardGame.getMaxRounds())); // LOADING
            cardGame.setRoundState(cardGame.getRoundState().nextPhase(cardGame.getMaxRounds())); // PLAYING

            // when
            cardGameService.selectCard(joinCode.getValue(), host.getName().value(), 0);

            // then
            assertThat(cardGame.getPlayerHands().findPlayerByName(host.getName())).isNotNull();
            assertThat(cardGame.getPlayerHands().totalHandSize()).isEqualTo(1);
        }

        @Test
        void 카드_선택_후_게임_상태_메시지가_전송된다() {
            // given
            Room room = roomQueryService.findByJoinCode(joinCode);
            CardGame cardGame = (CardGame) room.startNextGame(host.getName().value());
            cardGameService.start(cardGame, joinCode.value());

            // PLAYING 상태로 변경
            cardGame.setRoundState(cardGame.getRoundState().nextPhase(cardGame.getMaxRounds()));
            cardGame.setRoundState(cardGame.getRoundState().nextPhase(cardGame.getMaxRounds()));

            // when
            cardGameService.selectCard(joinCode.getValue(), host.getName().value(), 0);

            // then
            verify(messagingTemplate, atLeast(2)) // start() + selectCard() 호출
                    .convertAndSend(
                            eq("/topic/room/" + joinCode.getValue() + "/gameState"),
                            any(WebSocketResponse.class)
                    );
        }

        @Test
        void 만약_선택된_카드를_고르면_예외를_반환한다() {
            // given
            Room room = roomQueryService.findByJoinCode(joinCode);
            CardGame cardGame = (CardGame) room.startNextGame(host.getName().value());
            cardGameService.start(cardGame, joinCode.value());

            // PLAYING 상태로 변경
            cardGame.setRoundState(cardGame.getRoundState().nextPhase(cardGame.getMaxRounds()));
            cardGame.setRoundState(cardGame.getRoundState().nextPhase(cardGame.getMaxRounds()));

            List<Player> players = room.getPlayers();

            // when & then
            // 첫 번째 플레이어가 카드 선택
            cardGameService.selectCard(joinCode.getValue(), players.get(0).getName().value(), 0);

            // 두 번째 플레이어가 같은 카드 선택 시도 - 예외 발생해야 함
            assertThatThrownBy(() ->
                    cardGameService.selectCard(joinCode.getValue(), players.get(1).getName().value(), 0)
            ).isInstanceOf(IllegalStateException.class)
             .hasMessageContaining("이미 뽑은 카드입니다");
        }

        @Test
        void 게임이_플레이_상태가_아니면_예외를_반환한다() {
            // given
            Room room = roomQueryService.findByJoinCode(joinCode);
            CardGame cardGame = (CardGame) room.startNextGame(host.getName().value());
            cardGameService.start(cardGame, joinCode.value());
            // PLAYING 상태로 변경하지 않음 (LOADING 상태)

            // when & then
            assertThatThrownBy(() ->
                    cardGameService.selectCard(joinCode.getValue(), host.getName().value(), 0)
            ).isInstanceOf(IllegalStateException.class)
             .hasMessageContaining("현재 카드 선택 가능한 단계가 아닙니다");
        }

        @Test
        void 존재하지_않는_플레이어면_예외를_반환한다() {
            // given
            Room room = roomQueryService.findByJoinCode(joinCode);
            CardGame cardGame = (CardGame) room.startNextGame(host.getName().value());
            cardGameService.start(cardGame, joinCode.value());

            // PLAYING 상태로 변경
            cardGame.setRoundState(cardGame.getRoundState().nextPhase(cardGame.getMaxRounds()));
            cardGame.setRoundState(cardGame.getRoundState().nextPhase(cardGame.getMaxRounds()));

            // when & then
            assertThatThrownBy(() ->
                    cardGameService.selectCard(joinCode.getValue(), "존재하지않는플레이어", 0)
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessageContaining("사용자를 찾을 수 없습니다");
        }

        @Test
        void 잘못된_카드_인덱스면_예외를_반환한다() {
            // given
            Room room = roomQueryService.findByJoinCode(joinCode);
            CardGame cardGame = (CardGame) room.startNextGame(host.getName().value());
            cardGameService.start(cardGame, joinCode.value());

            // PLAYING 상태로 변경
            cardGame.setRoundState(cardGame.getRoundState().nextPhase(cardGame.getMaxRounds()));
            cardGame.setRoundState(cardGame.getRoundState().nextPhase(cardGame.getMaxRounds()));

            // when & then
            assertThatThrownBy(() ->
                    cardGameService.selectCard(joinCode.getValue(), host.getName().value(), 999)
            ).isInstanceOf(IndexOutOfBoundsException.class);
        }

        @Test
        void 모든_플레이어가_카드_선택하면_조기_전환이_트리거된다() {
            // given
            Room room = roomQueryService.findByJoinCode(joinCode);
            CardGame cardGame = (CardGame) room.startNextGame(host.getName().value());
            cardGameService.start(cardGame, joinCode.value());

            // PLAYING 상태로 변경
            cardGame.setRoundState(cardGame.getRoundState().nextPhase(cardGame.getMaxRounds()));
            cardGame.setRoundState(cardGame.getRoundState().nextPhase(cardGame.getMaxRounds()));

            List<Player> players = room.getPlayers();

            // when - 모든 플레이어가 서로 다른 카드 선택
            for (int i = 0; i < players.size(); i++) {
                cardGameService.selectCard(joinCode.getValue(), players.get(i).getName().value(), i);
            }

            // then
            assertThat(cardGame.allPlayersSelected()).isTrue();
            assertThat(cardGame.getPlayerHands().totalHandSize()).isEqualTo(players.size());
        }
    }

    @Nested
    class 모니터링 {

        @Test
        void 활성_방_수를_정확히_반환한다() {
            // given
            Room room = roomQueryService.findByJoinCode(joinCode);
            Playable miniGame = room.startNextGame(host.getName().value());

            int beforeCount = cardGameService.getActiveRoomCount();

            // when
            cardGameService.start(miniGame, joinCode.value());
            int afterCount = cardGameService.getActiveRoomCount();

            // then
            assertThat(afterCount).isEqualTo(beforeCount + 1);
        }
        
        @Test
        void 방_정리_후_활성_방_수가_감소한다() {
            // given
            Room room = roomQueryService.findByJoinCode(joinCode);
            Playable miniGame = room.startNextGame(host.getName().value());
            cardGameService.start(miniGame, joinCode.value());
            
            int beforeCount = cardGameService.getActiveRoomCount();

            // when
            cardGameService.cleanupRoom(joinCode.value());
            int afterCount = cardGameService.getActiveRoomCount();

            // then
            assertThat(afterCount).isEqualTo(beforeCount - 1);
        }
    }

    @Nested
    class 라운드_상태_관리 {
        
        @Test
        void 게임_시작_후_라운드_상태가_올바르게_설정된다() {
            // given
            Room room = roomQueryService.findByJoinCode(joinCode);
            CardGame cardGame = (CardGame) room.startNextGame(host.getName().value());
            
            // when
            cardGameService.start(cardGame, joinCode.value());
            
            // then
            assertThat(cardGame.getRoundState()).isNotNull();
            assertThat(cardGame.getCurrentRoundNumber()).isEqualTo(1);
            assertThat(cardGame.getCurrentPhase()).isNotEqualTo(RoundPhase.READY);
        }
        
        @Test
        void 최대_라운드_수가_올바르게_설정된다() {
            // given
            Room room = roomQueryService.findByJoinCode(joinCode);
            CardGame cardGame = (CardGame) room.startNextGame(host.getName().value());
            
            // when
            cardGameService.start(cardGame, joinCode.value());
            
            // then
            assertThat(cardGame.getMaxRounds()).isEqualTo(2); // 설정 파일의 기본값
        }
    }
}
