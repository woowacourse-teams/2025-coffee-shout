package coffeeshout.minigame.racinggame.application;

import coffeeshout.minigame.cardgame.domain.MiniGameType;
import coffeeshout.minigame.racinggame.domain.RacingGame;
import coffeeshout.minigame.racinggame.domain.event.RacingGameStarted;
import coffeeshout.minigame.racinggame.domain.event.RunnersMoved;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.service.RoomQueryService;
import java.time.Duration;
import java.util.concurrent.ScheduledFuture;
import lombok.RequiredArgsConstructor;
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
        startAutoMove(racingGame);

        eventPublisher.publishEvent(new RacingGameStarted(racingGame.getState().name()));
        log.info("레이싱 게임 시작 완료: joinCode={}", joinCode);
    }

    public void processTap(String joinCode, String playerName, int tapCount) {
        final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));
        final RacingGame racingGame = getRacingGame(room);
        final Player player = room.findPlayer(new PlayerName(playerName));
        racingGame.adjustSpeed(player, tapCount);

        log.debug("탭 처리 완료: joinCode={}, playerName={}, tapCount={}", joinCode, playerName, tapCount);
    }

    public void startAutoMove(RacingGame racingGame) {
        final ScheduledFuture<?> autoMoveFuture = taskScheduler.scheduleAtFixedRate(() -> {
            try {
                if (racingGame.isStarted()) {
                    racingGame.moveAll();
                    if (racingGame.isFinished()) {
                        racingGame.stopAutoMove();
                        return;
                    }
                    eventPublisher.publishEvent(RunnersMoved.from(racingGame));
                }
            } catch (Exception e) {
                racingGame.stopAutoMove();
            }
        }, Duration.ofMillis(RacingGame.MOVE_INTERVAL_MILLIS));
        racingGame.startAutoMove(autoMoveFuture);
    }

    private RacingGame getRacingGame(Room room) {
        return (RacingGame) room.findMiniGame(MiniGameType.RACING_GAME);
    }
}
