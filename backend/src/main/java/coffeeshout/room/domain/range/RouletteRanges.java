package coffeeshout.room.domain.range;

import coffeeshout.player.domain.Player;
import coffeeshout.room.domain.probability.PlayersWithProbability;
import java.util.ArrayList;
import java.util.List;

public class RouletteRanges {

    private final List<RouletteRange> ranges = new ArrayList<>();

    public RouletteRanges(PlayersWithProbability probabilities) {
        probabilities.forEach((player, probability) -> ranges.add(generateRange(
                calculateStartValue(),
                probability.value(),
                player
        )));
    }

    public Player pickPlayer(int number) {
        return ranges.stream().filter(rouletteRange -> rouletteRange.isBetween(number)).findFirst()
                .orElseThrow(() -> new IllegalStateException("범위에 해당하지 않는 숫자입니다."))
                .player();
    }

    public int calculateStartValue() {
        if (ranges.isEmpty()) {
            return 1;
        }
        return ranges.getLast().end() + 1;
    }

    private RouletteRange generateRange(int start, int gap, Player player) {
        return new RouletteRange(start, start + gap - 1, player);
    }

    public int endValue() {
        return ranges.getLast().end();
    }
}
