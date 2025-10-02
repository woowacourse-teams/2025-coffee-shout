package coffeeshout.minigame.racinggame.application;

import coffeeshout.minigame.cardgame.domain.MiniGameType;
import coffeeshout.minigame.racinggame.domain.RacingGame;
import coffeeshout.minigame.racinggame.domain.event.RaceFinishedEvent;
import coffeeshout.minigame.racinggame.domain.event.RaceStartedEvent;
import coffeeshout.minigame.racinggame.domain.event.RunnersMovedEvent;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.service.RoomQueryService;
import java.time.Duration;
import java.util.concurrent.ScheduledFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RacingGameCommandService {

    private final RoomQueryService roomQueryService;
    private final TaskScheduler taskScheduler;
    private final ApplicationEventPublisher eventPublisher;

    public RacingGameCommandService(
            RoomQueryService roomQueryService,
            @Qualifier("racingGameScheduler") TaskScheduler taskScheduler,
            ApplicationEventPublisher eventPublisher
    ) {
        this.roomQueryService = roomQueryService;
        this.taskScheduler = taskScheduler;
        this.eventPublisher = eventPublisher;
    }

    public void startGame(String joinCode, String hostName) {
        final JoinCode roomJoinCode = new JoinCode(joinCode);
        final Room room = roomQueryService.getByJoinCode(roomJoinCode);
        room.startNextGame(hostName);

        final RacingGame racingGame = getRacingGame(room);
        startAutoMove(racingGame, joinCode);

        eventPublisher.publishEvent(RaceStartedEvent.of(racingGame, joinCode));
        log.info("레이싱 게임 시작 완료: joinCode={}", joinCode);
    }

    public void processTap(String joinCode, String playerName, int tapCount) {
        final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));
        final RacingGame racingGame = getRacingGame(room);
        final Player player = room.findPlayer(new PlayerName(playerName));
        racingGame.adjustSpeed(player, tapCount);

        log.debug("탭 처리 완료: joinCode={}, playerName={}, tapCount={}", joinCode, playerName, tapCount);
    }

    private void startAutoMove(RacingGame racingGame, String joinCode) {
        final ScheduledFuture<?> autoMoveFuture = scheduleAutoMoveTask(racingGame, joinCode);
        racingGame.startAutoMove(autoMoveFuture);
    }

    private ScheduledFuture<?> scheduleAutoMoveTask(RacingGame racingGame, String joinCode) {
        return taskScheduler.scheduleAtFixedRate(() -> executeAutoMove(racingGame, joinCode),
                Duration.ofMillis(RacingGame.MOVE_INTERVAL_MILLIS));
    }

    private void executeAutoMove(RacingGame racingGame, String joinCode) {
        try {
            if (!racingGame.isStarted()) {
                return;
            }

            racingGame.moveAll();
            publishRunnersMoved(racingGame, joinCode);

            if (racingGame.isFinished()) {
                handleRaceFinished(racingGame, joinCode);
            }
        } catch (Exception e) {
            handleAutoMoveError(racingGame, e);
        }
    }

    private void handleRaceFinished(RacingGame racingGame, String joinCode) {
        racingGame.stopAutoMove();
        eventPublisher.publishEvent(RaceFinishedEvent.of(racingGame, joinCode));
        log.info("레이싱 게임 종료: joinCode={}", joinCode);
    }

    private void publishRunnersMoved(RacingGame racingGame, String joinCode) {
        eventPublisher.publishEvent(RunnersMovedEvent.from(racingGame, joinCode));
    }

    private void handleAutoMoveError(RacingGame racingGame, Exception e) {
        log.error("자동 이동 중 오류 발생", e);
        racingGame.stopAutoMove();
    }

    private RacingGame getRacingGame(Room room) {
        return (RacingGame) room.findMiniGame(MiniGameType.RACING_GAME);
    }
}
