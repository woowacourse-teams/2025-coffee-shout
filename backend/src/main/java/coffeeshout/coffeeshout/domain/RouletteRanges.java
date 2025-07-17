package coffeeshout.coffeeshout.domain;

import java.util.ArrayList;
import java.util.List;

public class RouletteRanges {

    private final List<RouletteRange> ranges = new ArrayList<>();

    public RouletteRanges(PlayersWithProbability probabilities) {
        probabilities.forEach((player, probability) -> ranges.add(generateRange(
                endValue() + 1,
                probability.value(),
                player)
        ));
    }

    public Player pickPlayer(int number) {
        return ranges.stream().filter(rouletteRange -> rouletteRange.isBetween(number)).findFirst()
                .orElseThrow(() -> new IllegalStateException("범위에 해당하지 않는 숫자입니다."))
                .player();
    }

    public int endValue() {
        if (ranges.isEmpty()) {
            return 0;
        }
        return ranges.getLast().end();
    }

    private RouletteRange generateRange(int start, int gap, Player player) {
        return new RouletteRange(start, start + gap, player);
    }
}
