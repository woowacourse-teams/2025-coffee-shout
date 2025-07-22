package coffeeshout.room.domain.roulette;

import coffeeshout.room.domain.player.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RouletteRanges {

    private final List<RouletteRange> ranges;

    public RouletteRanges(Map<Player, Probability> probabilities) {
        this.ranges = new ArrayList<>();

        probabilities.forEach((player, probability) -> ranges.add(generateRange(
                                endValue() + 1,
                                probability.value(),
                                player
                        )
                )
        );
    }

    public Player pickPlayer(int number) {
        return ranges.stream()
                .filter(rouletteRange -> rouletteRange.isBetween(number))
                .findFirst()
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
        return new RouletteRange(start, start + gap - 1, player);
    }
}
