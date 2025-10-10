package coffeeshout.dashboard.ui.response;

import java.util.List;

public record LowestProbabilityWinnerResponse(
        Integer probability,
        List<String> nicknames
) {
}
