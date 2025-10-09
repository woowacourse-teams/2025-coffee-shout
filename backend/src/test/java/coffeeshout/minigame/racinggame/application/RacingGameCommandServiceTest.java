package coffeeshout.minigame.racinggame.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.reset;

import coffeeshout.fixture.RoomFixture;
import coffeeshout.global.ServiceTest;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.racinggame.application.RacingGameCommandService;
import coffeeshout.racinggame.domain.RacingGame;
import coffeeshout.racinggame.domain.RacingGameState;
import coffeeshout.racinggame.domain.event.RaceStateChangedEvent;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.repository.RoomRepository;
import coffeeshout.room.domain.service.RoomQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;

class RacingGameCommandServiceTest extends ServiceTest {

    @Autowired
    @Qualifier("racingGameScheduler")
    private TaskScheduler taskScheduler;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomQueryService roomQueryService;

    private RacingGameCommandService racingGameCommandService;

    private static final String HOST_NAME = "꾹이";

    @BeforeEach
    void setUp() {
        reset(taskScheduler, eventPublisher);
        racingGameCommandService = new RacingGameCommandService(
                roomQueryService,
                taskScheduler,
                eventPublisher
        );
    }

    private Room createRoomWithRacingGame() {
        final Room room = RoomFixture.호스트_꾹이();
        room.addMiniGame(new PlayerName(HOST_NAME), new RacingGame());

        // 모든 플레이어를 준비 상태로 변경
        room.getPlayers().forEach(player -> player.updateReadyState(true));

        roomRepository.save(room);
        return room;
    }

    @Test
    void 레이싱_게임을_시작하면_상태가_PLAYING이_된다() {
        // given
        final Room room = createRoomWithRacingGame();
        given(taskScheduler.scheduleAtFixedRate(any(Runnable.class), any())).willReturn(null);

        // when
            racingGameCommandService.start(room.getJoinCode().getValue(), HOST_NAME);

        // then
        final RacingGame startedGame = (RacingGame) room.findMiniGame(MiniGameType.RACING_GAME);
        assertThat(startedGame.getState()).isEqualTo(RacingGameState.PLAYING);
        assertThat(startedGame.isStarted()).isTrue();
        assertThat(startedGame.isFinished()).isFalse();
        verify(taskScheduler).scheduleAtFixedRate(any(Runnable.class), any());
        verify(eventPublisher).publishEvent(any(RaceStateChangedEvent.class));
    }

    @Test
    void 레이싱_게임_시작시_자동_이동_스케줄링을_시작한다() {
        // given
        final Room room = createRoomWithRacingGame();
        given(taskScheduler.scheduleAtFixedRate(any(Runnable.class), any())).willReturn(null);

        // when
        racingGameCommandService.start(room.getJoinCode().getValue(), HOST_NAME);

        // then
        verify(taskScheduler).scheduleAtFixedRate(any(Runnable.class), any());
    }

    @Test
    void 탭_처리를_하면_게임_상태는_PLAYING을_유지한다() {
        // given
        final Room room = createRoomWithRacingGame();
        given(taskScheduler.scheduleAtFixedRate(any(Runnable.class), any())).willReturn(null);
        racingGameCommandService.start(room.getJoinCode().getValue(), HOST_NAME);

        final RacingGame racingGame = (RacingGame) room.findMiniGame(MiniGameType.RACING_GAME);
        final String playerName = "루키";
        final int tapCount = 5;

        assertThat(racingGame.getState()).isEqualTo(RacingGameState.PLAYING);

        // when
        racingGameCommandService.processTap(room.getJoinCode().getValue(), playerName, tapCount);

        // then
        assertThat(racingGame.getState()).isEqualTo(RacingGameState.PLAYING);
        assertThat(racingGame.isStarted()).isTrue();
        assertThat(racingGame.isFinished()).isFalse();
    }

    @Test
    void 게임_시작_전_상태는_DESCRIPTION이다() {
        // given
        final RacingGame racingGame = new RacingGame();

        // when & then
        assertThat(racingGame.getState()).isEqualTo(RacingGameState.DESCRIPTION);
        assertThat(racingGame.isStarted()).isFalse();
        assertThat(racingGame.isFinished()).isFalse();
    }
}
