package coffeeshout.minigame.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import coffeeshout.fixture.MenuFixture;
import coffeeshout.fixture.PlayersFixture;
import coffeeshout.global.ServiceTest;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.event.dto.CardGameStartedEvent;
import coffeeshout.minigame.domain.cardgame.event.dto.CardSelectedEvent;
import coffeeshout.room.application.RoomService;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.menu.MenuTemperature;
import coffeeshout.room.domain.menu.SelectedMenu;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.player.Players;
import coffeeshout.room.domain.service.RoomQueryService;
import coffeeshout.room.ui.request.SelectedMenuRequest;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CardGameServiceTest extends ServiceTest {

    @Autowired
    CardGameService cardGameService;

    @Autowired
    RoomQueryService roomQueryService;

    @Autowired
    RoomService roomService;

    JoinCode joinCode;

    Player host;

    Room room;

    CardGame cardGame;

    @BeforeEach
    void setUp() {
        Players players = PlayersFixture.호스트꾹이_루키_엠제이_한스;
        host = players.getPlayer(new PlayerName("꾹이"));
        room = roomService.createRoom(
                host.getName().value(),
                new SelectedMenuRequest(1L, null, MenuTemperature.ICE)
        );
        joinCode = room.getJoinCode();
        room.addMiniGame(host.getName(), MiniGameType.CARD_GAME.createMiniGame());

        for (int i = 1; i < players.getPlayers().size(); i++) {
            room.joinGuest(
                    players.getPlayers().get(i).getName(),
                    new SelectedMenu(MenuFixture.아메리카노(), MenuTemperature.ICE)
            );
        }
        for (Player player : room.getPlayers()) {
            player.updateReadyState(true);
        }
        cardGame = (CardGame) room.startNextGame(host.getName().value());
    }

    @Nested
    class 카드게임_시작 {

        @Test
        void 카드게임을_시작한다() {
            // given
            String joinCodeValue = joinCode.getValue();
            cardGameService.start(cardGame, joinCodeValue);

            // when & then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(cardGame).isNotNull();
                softly.assertThat(cardGame.getDeck().size()).isEqualTo(9);
                softly.assertThat(cardGame.getPlayerHands().playerCount()).isEqualTo(4);
                verify(eventPublisher).publishEvent(any(CardGameStartedEvent.class));
            });
        }
    }

    @Nested
    class 카드_선택 {

        @Test
        void 카드를_정상적으로_선택한다() {
            // given
            cardGame.startPlay();
            String joinCodeValue = joinCode.getValue();

            // when
            cardGameService.selectCard(joinCodeValue, host.getName().value(), 0);

            // then
            assertThat(cardGame.getPlayerHands().findPlayerByName(host.getName())).isNotNull();
        }

        @Test
        void 카드_선택_후_게임_상태_메시지가_전송된다() {
            // given
            cardGame.startPlay();
            String joinCodeValue = joinCode.getValue();

            // when
            cardGameService.selectCard(joinCodeValue, host.getName().value(), 0);

            // then
            verify(eventPublisher).publishEvent(any(CardSelectedEvent.class));
        }

        @Test
        void 만약_선택된_카드를_고르면_예외를_반환한다() {
            // given
            cardGame.startPlay();
            List<Player> players = room.getPlayers();

            // when & then
            // 첫 번째 플레이어가 카드 선택
            final String joinCodeValue = joinCode.getValue();
            cardGameService.selectCard(joinCodeValue, players.get(0).getName().value(), 0);

            // 두 번째 플레이어가 같은 카드 선택 시도 - 예외 발생해야 함

            final String secondPlayerName = players.get(1).getName().value();
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
            cardGame.startPlay();

            final String joinCodeValue = joinCode.getValue();

            // when & then
            assertThatThrownBy(() ->
                    cardGameService.selectCard(joinCodeValue, "존재하지않는플레이어", 0)
            ).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void 잘못된_카드_인덱스면_예외를_반환한다() {
            // given
            cardGame.startPlay();
            final String joinCodeValue = joinCode.getValue();
            final String hostName = host.getName().value();

            // when & then
            assertThatThrownBy(() ->
                    cardGameService.selectCard(joinCodeValue, hostName, 999)
            ).isInstanceOf(IndexOutOfBoundsException.class);
        }
    }
}
