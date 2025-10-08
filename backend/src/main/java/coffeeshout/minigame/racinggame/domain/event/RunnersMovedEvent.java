package coffeeshout.minigame.racinggame.domain.event;

import coffeeshout.minigame.racinggame.domain.RacingGame;
import coffeeshout.minigame.racinggame.domain.dto.RacingRange;
import coffeeshout.minigame.racinggame.domain.dto.RunnerPosition;
import java.util.List;

public record RunnersMovedEvent(String joinCode, RacingRange racingRange, List<RunnerPosition> runnerPositions) {

    public static RunnersMovedEvent from(RacingGame racingGame, String joinCode) {
        final RacingRange distance = new RacingRange(RacingGame.START_LINE, RacingGame.FINISH_LINE);
        final List<RunnerPosition> positions = racingGame.getRunners().stream()
                .map(runner -> new RunnerPosition(
                        runner.getPlayer().getName().value(),
                        runner.getPosition(),
                        runner.getSpeed()
                )).toList();
        return new RunnersMovedEvent(joinCode, distance, positions);
    }
}
