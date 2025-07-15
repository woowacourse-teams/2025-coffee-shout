package coffeeshout.ui.response;

import java.util.Map;

public record GameStateMessage(
        Long roomId,
        int currentRound,
        Map<CardDto, Long> playerSelections,
        Map<Long, Integer> scores,
        Boolean allSelected
) {
}
