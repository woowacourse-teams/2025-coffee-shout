package coffeeshout.minigame.racinggame.domain;

import coffeeshout.minigame.cardgame.domain.MiniGameResult;
import coffeeshout.minigame.cardgame.domain.MiniGameScore;
import coffeeshout.minigame.cardgame.domain.MiniGameType;
import coffeeshout.room.domain.Playable;
import coffeeshout.room.domain.player.Player;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.Getter;

@Getter
public class RacingGame implements Playable {

    private static final long MOVE_INTERVAL_MILLIS = 100L;

    private Runners runners;
    private RacingGameState state;
    private ScheduledExecutorService scheduler;

    public RacingGame() {
        this.state = RacingGameState.READY;
    }

    @Override
    public void startGame(List<Player> players) {
        this.runners = new Runners(players);
        this.state = RacingGameState.PLAYING;
        startAutoMove();
    }

    private void startAutoMove() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (state == RacingGameState.PLAYING) {
                    runners.moveAll();
                    if (runners.isAllFinished()) {
                        stopAutoMove();
                        this.state = RacingGameState.FINISHED;
                    }
                }
            } catch (Exception e) {
                // 예외 발생 시 스케줄러 중단
                stopAutoMove();
            }
        }, MOVE_INTERVAL_MILLIS, MOVE_INTERVAL_MILLIS, TimeUnit.MILLISECONDS);
    }

    private void stopAutoMove() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    public void adjustSpeed(Player player, int tapCount, Instant timestamp) {
        validatePlaying();
        runners.adjustSpeed(player, tapCount, timestamp);
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
        Map<Player, Integer> positions = runners.getPositions();
        return positions.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> new MiniGameScore(entry.getValue())
                ));
    }

    @Override
    public MiniGameType getMiniGameType() {
        return MiniGameType.RACING_GAME;
    }

    public List<Player> getRanking() {
        return runners.getRanking();
    }

    public Map<Player, Integer> getPositions() {
        return runners.getPositions();
    }

    public Map<Player, Integer> getSpeeds() {
        return runners.getSpeeds();
    }

    public boolean isFinished() {
        return state == RacingGameState.FINISHED;
    }
}
