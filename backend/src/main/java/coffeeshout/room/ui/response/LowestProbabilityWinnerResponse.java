package coffeeshout.room.ui.response;

import java.util.List;

public record LowestProbabilityWinnerResponse(
        Integer probability,
        List<String> nicknames
) {
}
