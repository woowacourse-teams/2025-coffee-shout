package coffeeshout.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import coffeeshout.Fixture.CardGameDeckStub;
import coffeeshout.Fixture.PlayerFixture;
import coffeeshout.domain.CardGame;
import coffeeshout.domain.CardGameRound;
import coffeeshout.domain.GeneralCard;
import coffeeshout.domain.MiniGameResult;
import coffeeshout.domain.Player;
import coffeeshout.domain.Room;
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
    RoomQueryService roomQueryService;

    @Mock
    PlayerQueryService playerQueryService;

    @InjectMocks
    CardGameService cardGameService;

    Long roomId;

    List<Player> players;

    @BeforeEach
    void setUp() {
        roomId = 1L;
        players = PlayerFixture.getPlayers();

        ConcurrentHashMap<Long, CardGame> cardGames = new ConcurrentHashMap<>();
        cardGames.put(roomId, new CardGame(players, new CardGameDeckStub()));

        ReflectionTestUtils.setField(cardGameService, "cardGames", cardGames);
    }

    @Test
    void 게임을_시작한다() {
        // given
        long roomId = 2L;
        Room room = new Room();
        ReflectionTestUtils.setField(room, "players", PlayerFixture.getPlayers());
        when(roomQueryService.findById(roomId)).thenReturn(room);

        // when
        cardGameService.start(roomId);

        // then
        CardGame cardGame = cardGameService.getCardGame(roomId);

        assertThat(cardGame.getPlayerCards()).hasSize(4);
        verify(roomQueryService).findById(roomId);
    }

    @Test
    void 카드를_고른다() {
        // given
        long playerId = 1L;

        when(playerQueryService.findById(playerId)).thenReturn(players.getFirst());

        // when
        cardGameService.selectCard(roomId, playerId, 0);

        // then
        assertThat(cardGameService.getCardGame(roomId).getPlayerCards().get(players.getFirst())).hasSize(1);
        assertThat(cardGameService.getCardGame(roomId).getPlayerCards().get(players.getFirst()).getFirst()).isEqualTo(
                new GeneralCard(40));
    }

    @Test
    void 라운드1이_종료되었는지_검사한다() {
        // given
        when(playerQueryService.findById(1L)).thenReturn(players.get(0));
        when(playerQueryService.findById(2L)).thenReturn(players.get(1));
        when(playerQueryService.findById(3L)).thenReturn(players.get(2));
        when(playerQueryService.findById(4L)).thenReturn(players.get(3));

        cardGameService.selectCard(roomId, players.get(0).getId(), 0);
        cardGameService.selectCard(roomId, players.get(1).getId(), 1);
        cardGameService.selectCard(roomId, players.get(2).getId(), 2);
        cardGameService.selectCard(roomId, players.get(3).getId(), 3);

        // when
        cardGameService.checkRound(roomId);

        // then
        assertThat(cardGameService.getCardGame(roomId).getRound()).isEqualTo(CardGameRound.TWO);
    }

    @Test
    void 라운드2가_종료되었는지_검사한다() {
        // given
        when(playerQueryService.findById(1L)).thenReturn(players.get(0));
        when(playerQueryService.findById(2L)).thenReturn(players.get(1));
        when(playerQueryService.findById(3L)).thenReturn(players.get(2));
        when(playerQueryService.findById(4L)).thenReturn(players.get(3));

        cardGameService.selectCard(roomId, players.get(0).getId(), 0);
        cardGameService.selectCard(roomId, players.get(1).getId(), 1);
        cardGameService.selectCard(roomId, players.get(2).getId(), 2);
        cardGameService.selectCard(roomId, players.get(3).getId(), 3);
        cardGameService.checkRound(roomId);

        cardGameService.selectCard(roomId, players.get(0).getId(), 4);
        cardGameService.selectCard(roomId, players.get(1).getId(), 5);
        cardGameService.selectCard(roomId, players.get(2).getId(), 6);
        cardGameService.selectCard(roomId, players.get(3).getId(), 7);

        // when
        cardGameService.checkRound(roomId);

        // then
        assertThat(cardGameService.getCardGame(roomId).getRound()).isEqualTo(CardGameRound.END);
    }

    @Test
    void 게임결과를_계산한다() {
        // given
        when(playerQueryService.findById(1L)).thenReturn(players.get(0));
        when(playerQueryService.findById(2L)).thenReturn(players.get(1));
        when(playerQueryService.findById(3L)).thenReturn(players.get(2));
        when(playerQueryService.findById(4L)).thenReturn(players.get(3));

        cardGameService.selectCard(roomId, players.get(1).getId(), 1);
        cardGameService.selectCard(roomId, players.get(2).getId(), 2);
        cardGameService.selectCard(roomId, players.get(3).getId(), 3);
        cardGameService.selectCard(roomId, players.get(0).getId(), 0);
        cardGameService.checkRound(roomId);

        cardGameService.selectCard(roomId, players.get(0).getId(), 4);
        cardGameService.selectCard(roomId, players.get(1).getId(), 5);
        cardGameService.selectCard(roomId, players.get(2).getId(), 6);
        cardGameService.selectCard(roomId, players.get(3).getId(), 7);
        cardGameService.checkRound(roomId);

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
}
