package coffeeshout.dashboard.domain;

import java.util.List;

public record LowestProbabilityWinnerResponse(
        Integer probability,
        List<String> playerNames
) {
}
