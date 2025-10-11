package coffeeshout.racinggame.ui.response;

import java.util.List;

public record RacingGameRunnersStateResponse (RacingRange distance, List<RunnerPosition> players) {

}
