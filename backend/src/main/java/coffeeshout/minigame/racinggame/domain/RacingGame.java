package coffeeshout.minigame.racinggame.domain;

import coffeeshout.minigame.cardgame.domain.MiniGameResult;
import coffeeshout.minigame.cardgame.domain.MiniGameScore;
import coffeeshout.minigame.cardgame.domain.MiniGameType;
import coffeeshout.room.domain.Playable;
import coffeeshout.room.domain.player.Player;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.Getter;
import org.springframework.scheduling.TaskScheduler;

@Getter
public class RacingGame implements Playable {

    public static final int INITIAL_SPEED = 5;
    public static final int MIN_SPEED = 1;
    public static final int MAX_SPEED = 10;
    public static final int FINISH_LINE = 1000;
    public static final int START_LINE = 0;
    public static final double CLICKS_PER_SECOND_THRESHOLD = 10.0;

    public static final long MOVE_INTERVAL_MILLIS = 100L;

    private Runners runners;
    private RacingGameState state;
    private ScheduledFuture<?> autoMoveFuture;

    public RacingGame() {
        this.state = RacingGameState.READY;
    }

    @Override
    public void startGame(List<Player> players) {
        this.runners = new Runners(players);
        this.state = RacingGameState.PLAYING;
    }

    public void startAutoMove(ScheduledFuture<?> autoMoveFuture) {
        this.autoMoveFuture = autoMoveFuture;
    }

    public void moveAll() {
        runners.moveAll();
        if (runners.isAllFinished()) {
            this.state = RacingGameState.FINISHED;
        }
    }

    public boolean isStarted() {
        return state == RacingGameState.PLAYING;
    }

    public void stopAutoMove() {
        if (autoMoveFuture != null && !autoMoveFuture.isDone()) {
            autoMoveFuture.cancel(true);
        }
    }

    public void adjustSpeed(Player player, int tapCount) {
        validatePlaying();
        runners.adjustSpeed(player, tapCount);
    }

    private void validatePlaying() {
        if (state != RacingGameState.PLAYING) {
            throw new IllegalStateException("게임이 진행 중이 아닙니다.");
        }
    }

    @Override
    public MiniGameResult getResult() {
        return MiniGameResult.from(getScores());
    }

    @Override
    public Map<Player, MiniGameScore> getScores() {
        final List<Runner> positions = runners.getRanking();

        return positions.stream()
                .collect(Collectors.toMap(
                        Runner::getPlayer,
                        entry -> new RacingGameScore(entry.getFinishTime())
                ));
    }

    @Override
    public MiniGameType getMiniGameType() {
        return MiniGameType.RACING_GAME;
    }

    public List<Player> getRanking() {
        return runners.getRanking().stream().map(Runner::getPlayer).toList();
    }

    public Map<Runner, Integer> getPositions() {
        return runners.getPositions();
    }

    public Map<Runner, Integer> getSpeeds() {
        return runners.getSpeeds();
    }


    public boolean isFinished() {
        return state == RacingGameState.FINISHED;
    }
}
