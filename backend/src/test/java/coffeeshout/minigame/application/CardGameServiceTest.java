package coffeeshout.minigame.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import coffeeshout.fixture.CardGameDeckStub;
import coffeeshout.fixture.PlayersFixture;
import coffeeshout.fixture.RoomFixture;
import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.CardGameRound;
import coffeeshout.minigame.domain.cardgame.CardGameState;
import coffeeshout.minigame.ui.MiniGameStateMessage;
import coffeeshout.player.domain.Player;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.RoomFinder;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CardGameServiceTest {

    @Mock
    private RoomFinder roomFinder;

    @Mock
    private CardGameRepository cardGameRepository;

    @Mock
    private RoomTimers roomTimers;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private CardGameService cardGameService;

    private Room room;
    private Long roomId;
    private List<Player> players;
    private CardGame cardGame;

    @BeforeEach
    void setUp() {
        players = PlayersFixture.꾹이_루키_엠제이_한스().getPlayers();
        roomId = 1L;
        room = RoomFixture.호스트_꾹이();
        players.stream()
                .filter(player -> !player.equals(room.getHost()))
                .forEach(player -> room.joinPlayer(player));

        cardGame = new CardGame(new CardGameDeckStub(), room.getPlayers());
        cardGame.startRound();
    }

    @Test
    void 게임을_시작한다() {
        // given
        cardGame = new CardGame(new CardGameDeckStub(), room.getPlayers());
        when(roomFinder.findById(roomId)).thenReturn(room);
        when(cardGameRepository.save(eq(roomId), any(CardGame.class))).thenReturn(cardGame);
        when(cardGameRepository.findByRoomId(roomId)).thenReturn(Optional.of(cardGame));

        // when
        cardGameService.startGame(roomId);

        // then
        SoftAssertions.assertSoftly(softly -> {
            verify(roomFinder).findById(roomId);
            verify(cardGameRepository).save(eq(roomId), any(CardGame.class));
            verify(messagingTemplate).convertAndSend(
                    eq("/topic/room/1/gameState"),
                    any(MiniGameStateMessage.class)
            );
            verify(roomTimers).start(eq(roomId), any(Runnable.class), anyInt());
        });
    }

    @Test
    void _카드를_선택한다() {
        // given
        String playerName = "루키";
        Integer cardIndex = 0;

        cardGame.startRound();
        when(cardGameRepository.findByRoomId(roomId)).thenReturn(Optional.of(cardGame));

        // when
        cardGameService.selectCard(roomId, playerName, cardIndex);

        // then
        SoftAssertions.assertSoftly(softly -> {
            assertThat(cardGame.getPlayerHands().totalHandSize()).isEqualTo(1);
            verify(messagingTemplate).convertAndSend(
                    eq("/topic/room/1/gameState"),
                    any(MiniGameStateMessage.class)
            );
        });
    }

    @Test
    void 라운드가_완료되면_스코어보드로_전환된다() {
        // given
        when(cardGameRepository.findByRoomId(roomId)).thenReturn(Optional.of(cardGame));

        // when
        selectAllPlayersCards();

        // then
        SoftAssertions.assertSoftly(softly -> {
            assertThat(cardGame.getState()).isEqualTo(CardGameState.SCORE_BOARD);
            verify(roomTimers).cancel(roomId);
            verify(messagingTemplate, times(6)).convertAndSend(
                    eq("/topic/room/1/gameState"),
                    any(MiniGameStateMessage.class)
            );
        });
    }

    @Test
    void 타임아웃시_선택하지_않은_플레이어들에게_랜덤_카드가_배정된다() {
        // given
        cardGame.startRound();
        when(cardGameRepository.findByRoomId(roomId)).thenReturn(Optional.of(cardGame));
        when(roomFinder.findById(roomId)).thenReturn(room);
        cardGameService.startGame(roomId);

        ArgumentCaptor<Runnable> timeoutCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(roomTimers).start(eq(roomId), timeoutCaptor.capture(), anyInt());

        // when
        Runnable timeoutTask = timeoutCaptor.getValue();
        timeoutTask.run();

        // then
        assertThat(cardGame.getPlayerHands().totalHandSize()).isEqualTo(4);
        assertThat(cardGame.getState()).isEqualTo(CardGameState.SCORE_BOARD);
    }

    @Test
    void 라운드_완료_후_점수판_상태로_전환된다() {
        // given
        cardGame.startRound();
        when(cardGameRepository.findByRoomId(roomId)).thenReturn(Optional.of(cardGame));

        // when
        selectAllPlayersCards();

        // then
        assertThat(cardGame.getState()).isEqualTo(CardGameState.SCORE_BOARD);
    }

    void 점수판_상태_이후_로딩_상태로_전환된다() {
        // given
        when(cardGameRepository.findByRoomId(roomId)).thenReturn(Optional.of(cardGame));
        selectAllPlayersCards();

        // when
        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(roomTimers).start(eq(roomId), captor.capture(), anyInt());
        Runnable task = captor.getValue();
        task.run();

        // then
        assertThat(cardGame.getState()).isEqualTo(CardGameState.LOADING);
    }

    @Test
    void 두_번째_라운드_완료_후_게임이_종료된다() {
        // given
        when(cardGameRepository.findByRoomId(roomId)).thenReturn(Optional.of(cardGame));
        selectAllPlayersCards();

        ArgumentCaptor<Runnable> firstScoreBoardCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(roomTimers, times(1)).start(eq(roomId), firstScoreBoardCaptor.capture(), anyInt());
        firstScoreBoardCaptor.getValue().run();

        ArgumentCaptor<Runnable> loadingCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(roomTimers, times(2)).start(eq(roomId), loadingCaptor.capture(), anyInt());
        loadingCaptor.getValue().run();

        ReflectionTestUtils.setField(cardGame, "deck", new CardGameDeckStub().generate(6, 3));
        selectAllPlayersCards();

        // when
        ArgumentCaptor<Runnable> secondScoreBoardCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(roomTimers, times(4)).start(eq(roomId), secondScoreBoardCaptor.capture(), anyInt());
        secondScoreBoardCaptor.getValue().run();

        // then
        assertThat(cardGame.getRound()).isEqualTo(CardGameRound.SECOND);
        assertThat(cardGame.getState()).isEqualTo(CardGameState.SCORE_BOARD);
    }

    @Test
    void 게임_결과를_반환한다() {
        // given
        // round 1
        when(cardGameRepository.findByRoomId(roomId)).thenReturn(Optional.of(cardGame));
        selectAllPlayersCards();

        ArgumentCaptor<Runnable> firstScoreBoardCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(roomTimers, times(1)).start(eq(roomId), firstScoreBoardCaptor.capture(), anyInt());
        firstScoreBoardCaptor.getValue().run();

        ArgumentCaptor<Runnable> loadingCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(roomTimers, times(2)).start(eq(roomId), loadingCaptor.capture(), anyInt());
        loadingCaptor.getValue().run();

        // round 2
        ReflectionTestUtils.setField(cardGame, "deck", new CardGameDeckStub().generate(6, 3));
        selectAllPlayersCards();

        ArgumentCaptor<Runnable> secondScoreBoardCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(roomTimers, times(4)).start(eq(roomId), secondScoreBoardCaptor.capture(), anyInt());
        secondScoreBoardCaptor.getValue().run();

        // when
        MiniGameResult miniGameResult = cardGameService.getMiniGameResult(roomId);

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(miniGameResult.getRank()).hasSize(4);
            softly.assertThat(miniGameResult.getRank().entrySet()).containsExactlyInAnyOrder(
                    Map.entry(players.get(3), 4),
                    Map.entry(players.get(2), 3),
                    Map.entry(players.get(1), 2),
                    Map.entry(players.get(0), 1)
            );
        });
    }

    @Test
    void 존재하지_않는_룸ID로_조회시_예외가_발생한다() {
        // given
        when(cardGameRepository.findByRoomId(roomId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> cardGameService.getCardGame(roomId))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("해당 룸에는 카드게임이 존재하지 않습니다.");
    }

    @Test
    void 게임_시작시_상태_메시지가_전송된다() {
        // given
        cardGame.startRound();
        when(cardGameRepository.findByRoomId(roomId)).thenReturn(Optional.of(cardGame));
        when(roomFinder.findById(roomId)).thenReturn(room);

        // when
        cardGameService.startGame(roomId);

        // then
        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MiniGameStateMessage> messageCaptor = ArgumentCaptor.forClass(MiniGameStateMessage.class);

        verify(messagingTemplate).convertAndSend(
                destinationCaptor.capture(),
                messageCaptor.capture()
        );

        assertThat(destinationCaptor.getValue()).isEqualTo("/topic/room/1/gameState");
        assertThat(messageCaptor.getValue()).isNotNull();
    }

    @Test
    void 스코어보드_전환시_상태와_결과_메시지가_모두_전송된다() {
        // given
        cardGame.startRound();
        when(cardGameRepository.findByRoomId(roomId)).thenReturn(Optional.of(cardGame));

        // when
        selectAllPlayersCards();

        // then
        verify(messagingTemplate, times(6)).convertAndSend(
                eq("/topic/room/1/gameState"),
                any(MiniGameStateMessage.class)
        );
    }

    @Test
    void 라운드_완료시_타이머가_취소된다() {
        // given
        cardGame.startRound();
        when(cardGameRepository.findByRoomId(roomId)).thenReturn(Optional.of(cardGame));

        // when
        selectAllPlayersCards();

        // then
        verify(roomTimers).cancel(roomId);
    }

    private void selectAllPlayersCards() {
        for (int i = 0; i < players.size(); i++) {
            cardGameService.selectCard(roomId, players.get(i).getName(), i);
        }
    }
}
