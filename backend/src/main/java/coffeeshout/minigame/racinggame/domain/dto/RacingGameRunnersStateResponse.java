package coffeeshout.minigame.racinggame.domain.dto;

import java.util.List;

public record RacingGameRunnersStateResponse (RacingRange distance, List<RunnerPosition> runnerPosition) {

}
