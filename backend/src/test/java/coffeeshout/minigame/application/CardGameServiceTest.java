package coffeeshout.minigame.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import coffeeshout.fixture.CardGameDeckStub;
import coffeeshout.fixture.PlayerFixture;
import coffeeshout.fixture.PlayersFixture;
import coffeeshout.fixture.RoomFixture;
import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.minigame.domain.cardgame.AdditionCard;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.CardGameRound;
import coffeeshout.player.domain.Player;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.RoomFinder;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CardGameServiceTest {

    @Mock
    RoomFinder roomFinder;

    @InjectMocks
    CardGameService cardGameService;

    Room room;

    Long roomId;

    List<Player> players;

    @BeforeEach
    void setUp() {
        players = PlayersFixture.꾹이_루키_엠제이_한스().getPlayers();
        roomId = 1L;
        room = RoomFixture.호스트_꾹이();

        for (Player player : players) {
            if (player.equals(PlayerFixture.꾹이())) {
                continue;
            }
            room.joinPlayer(player);
        }

        ConcurrentHashMap<Long, CardGame> cardGames = new ConcurrentHashMap<>();
        CardGame cardGame = new CardGame(players, new CardGameDeckStub());
        cardGame.start();
        cardGames.put(roomId, cardGame);
        ReflectionTestUtils.setField(cardGameService, "cardGames", cardGames);
    }

    @Test
    void 게임을_시작한다() {
        // given
        long roomId = 2L;
        when(roomFinder.findById(roomId)).thenReturn(room);

        // when
        cardGameService.start(roomId);

        // then
        CardGame cardGame = cardGameService.getCardGame(roomId);

        assertThat(cardGame.getPlayerCards()).hasSize(4);
        verify(roomFinder).findById(roomId);
    }

    @Test
    void 카드를_고른다() {
        // given
        String playerName = "루키";
        Player 루키 = players.stream().filter(p -> p.getName().equals(playerName)).findFirst().get();

        // when
        cardGameService.selectCard(roomId, playerName, 0);

        // then
        assertThat(cardGameService.getCardGame(roomId).getPlayerCards().get(루키)).hasSize(1);
        assertThat(cardGameService.getCardGame(roomId).getPlayerCards().get(루키).getFirst()).isEqualTo(
                new AdditionCard(40));
    }

    @Test
    void 라운드1이_종료되었는지_검사한다() {
        // given
        selectFirstCardEveryPlayer();

        // when
        cardGameService.checkAndMoveRound(roomId);

        // then
        assertThat(cardGameService.getCardGame(roomId).getRound()).isEqualTo(CardGameRound.SECOND);
    }

    @Test
    void 라운드2가_종료되었는지_검사한다() {
        // given
        selectFirstCardEveryPlayer();
        cardGameService.checkAndMoveRound(roomId);

        selectSecondCardEveryPlayer();

        // when
        cardGameService.checkAndMoveRound(roomId);

        // then
        assertThat(cardGameService.getCardGame(roomId).getRound()).isEqualTo(CardGameRound.END);
    }

    @Test
    void 게임결과를_계산한다() {
        // given
        selectFirstCardEveryPlayer();
        cardGameService.checkAndMoveRound(roomId);

        selectSecondCardEveryPlayer();
        cardGameService.checkAndMoveRound(roomId);

        // when
        MiniGameResult miniGameResult = cardGameService.getMiniGameResult(roomId);

        // then
        /*
        0번 플레이어: 40 + 0 = 40 (2등),
        1번 플레이어: 30 - 10 = 20 (3등),
        2번 플레이어: 20 * 4 = 80 (1등),
        3번 플레이어: 10 * 2 = 20 (3등)
        * */
        assertThat(miniGameResult.getRank().get(players.get(0))).isEqualTo(2);
        assertThat(miniGameResult.getRank().get(players.get(1))).isEqualTo(3);
        assertThat(miniGameResult.getRank().get(players.get(2))).isEqualTo(1);
        assertThat(miniGameResult.getRank().get(players.get(3))).isEqualTo(3);
    }

    private void selectFirstCardEveryPlayer() {
        cardGameService.selectCard(roomId, players.get(0).getName(), 0);
        cardGameService.selectCard(roomId, players.get(1).getName(), 1);
        cardGameService.selectCard(roomId, players.get(2).getName(), 2);
        cardGameService.selectCard(roomId, players.get(3).getName(), 3);
    }

    private void selectSecondCardEveryPlayer() {
        cardGameService.selectCard(roomId, players.get(0).getName(), 4);
        cardGameService.selectCard(roomId, players.get(1).getName(), 5);
        cardGameService.selectCard(roomId, players.get(2).getName(), 6);
        cardGameService.selectCard(roomId, players.get(3).getName(), 7);
    }
}
