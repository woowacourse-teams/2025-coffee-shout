package coffeeshout.racinggame.domain.dto;

import java.util.List;

public record RacingGameRunnersStateResponse (RacingRange distance, List<RunnerPosition> players) {

}
