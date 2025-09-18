package coffeeshout.minigame.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

import coffeeshout.fixture.PlayersFixture;
import coffeeshout.global.ServiceTest;
import coffeeshout.global.ui.WebSocketResponse;
import coffeeshout.minigame.common.task.TaskManager;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.CardGameTaskExecutors;
import coffeeshout.minigame.domain.cardgame.CardGameTaskType;
import coffeeshout.minigame.domain.cardgame.card.CardGameRandomDeckGenerator;
import coffeeshout.minigame.domain.cardgame.service.CardGameCommandService;
import coffeeshout.minigame.domain.cardgame.service.CardGameQueryService;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.player.Players;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CardGameServiceTest extends ServiceTest {

    @Autowired
    CardGameService cardGameService;

    @Autowired
    CardGameTaskExecutors cardGameTaskExecutors;

    @Autowired
    CardGameQueryService cardGameQueryService;

    @Autowired
    CardGameCommandService cardGameCommandService;

    JoinCode joinCode;

    Players players;

    Player host;

    @BeforeEach
    void setUp() {
        players = PlayersFixture.호스트꾹이_루키_엠제이_한스();
        host = players.getPlayer(new PlayerName("꾹이"));
        joinCode = new JoinCode("A4B5N");
    }

    @Nested
    class 카드게임_시작 {

        @Test
        void 카드게임을_시작한다() {
            // given
            final JoinCode joinCode = new JoinCode("A5B7J");
            final String joinCodeValue = joinCode.getValue();
            cardGameService.start(joinCodeValue, players.getPlayers());

            final CardGame cardGame = cardGameQueryService.getByJoinCode(joinCode);

            // when & then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(cardGame).isNotNull();
                softly.assertThat(cardGame.getDeck().size()).isEqualTo(9);
                softly.assertThat(cardGame.getPlayerHands().playerCount()).isEqualTo(4);

                TaskManager<CardGameTaskType> executor = cardGameTaskExecutors.get(joinCode);
                softly.assertThat(executor).isNotNull();
            });
        }


        @Test
        void 카드게임을_시작하면_태스크가_순차적으로_실행된다() {
            // when
            cardGameService.start(joinCode.getValue(), players.getPlayers());

            // then
            await().atMost(3, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        verify(messagingTemplate, atLeast(6))
                                .convertAndSend(
                                        eq("/topic/room/" + joinCode.getValue() + "/gameState"),
                                        any(WebSocketResponse.class)
                                );
                    });

        }
    }

    @Nested
    class 카드_선택 {

        @Test
        void 카드를_정상적으로_선택한다() {
            // given
            savePlayingStateCardGame();
            final String joinCodeValue = joinCode.getValue();

            // when
            cardGameService.selectCard(joinCodeValue, host.getName().value(), 0);

            // then
            final CardGame cardGame = cardGameQueryService.getByJoinCode(joinCode);
            assertThat(cardGame.getPlayerHands().findPlayerByName(host.getName())).isNotNull();
        }

        @Test
        void 카드_선택_후_게임_상태_메시지가_전송된다() {
            // given
            savePlayingStateCardGame();
            final String joinCodeValue = joinCode.getValue();

            // when
            cardGameService.selectCard(joinCodeValue, host.getName().value(), 0);

            // then
            verify(messagingTemplate).convertAndSend(
                    eq("/topic/room/" + joinCodeValue + "/gameState"),
                    any(WebSocketResponse.class)
            );
        }

        @Test
        void 만약_선택된_카드를_고르면_예외를_반환한다() {
            // given
            savePlayingStateCardGame();
            final String joinCodeValue = joinCode.getValue();
            final List<Player> playerList = players.getPlayers();

            // when & then
            // 첫 번째 플레이어가 카드 선택
            cardGameService.selectCard(joinCodeValue, playerList.get(0).getName().value(), 0);

            // 두 번째 플레이어가 같은 카드 선택 시도 - 예외 발생해야 함
            final String secondPlayerName = playerList.get(1).getName().value();
            assertThatThrownBy(() ->
                    cardGameService.selectCard(joinCodeValue, secondPlayerName, 0)
            ).isInstanceOf(IllegalStateException.class);
        }

        @Test
        void 게임이_플레이_상태가_아니면_예외를_반환한다() {
            // when & then
            final String name = host.getName().value();
            final String joinCodeValue = joinCode.getValue();

            assertThatThrownBy(() ->
                    cardGameService.selectCard(joinCodeValue, name, 0)
            ).isInstanceOf(IllegalStateException.class);
        }

        @Test
        void 존재하지_않는_플레이어면_예외를_반환한다() {
            // given
            savePlayingStateCardGame();
            final String joinCodeValue = joinCode.getValue();

            // when & then
            assertThatThrownBy(() ->
                    cardGameService.selectCard(joinCodeValue, "존재하지않는플레이어", 0)
            ).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void 잘못된_카드_인덱스면_예외를_반환한다() {
            // given
            savePlayingStateCardGame();
            final String joinCodeValue = joinCode.getValue();
            final String hostName = host.getName().value();

            // when & then
            assertThatThrownBy(() ->
                    cardGameService.selectCard(joinCodeValue, hostName, 999)
            ).isInstanceOf(IndexOutOfBoundsException.class);
        }

        private void savePlayingStateCardGame() {
            final CardGame cardGame = new CardGame(players.getPlayers(), joinCode, new CardGameRandomDeckGenerator());
            cardGame.startPlay();
            cardGameCommandService.save(cardGame);
        }
    }
}
