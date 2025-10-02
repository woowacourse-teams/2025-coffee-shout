package coffeeshout.minigame.racinggame.domain.event;

import coffeeshout.minigame.racinggame.domain.RacingGame;
import java.util.List;

public record RunnersMoved(Distance distance, List<RunnerPosition> runnerPositions) {

    record Distance(int start, int end) {
    }

    record RunnerPosition(String playerName, int position, int speed) {
    }

    public static RunnersMoved from(RacingGame racingGame) {
        final Distance distance = new Distance(RacingGame.START_LINE, RacingGame.FINISH_LINE);
        final List<RunnerPosition> positions = racingGame.getRunners().stream()
                .map(runner -> new RunnerPosition(
                        runner.getPlayer().getPlayerType().name(),
                        runner.getPosition(),
                        runner.getSpeed()
                )).toList();
        return new RunnersMoved(distance, positions);
    }
}
