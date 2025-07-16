package coffeeshout.coffeeshout.domain.roulette;

import coffeeshout.coffeeshout.domain.player.Player;
import coffeeshout.coffeeshout.domain.player.PlayersWithProbability;
import java.util.ArrayList;
import java.util.List;

public class RouletteRanges {

    private final List<RouletteRange> values = new ArrayList<>();

    public RouletteRanges(PlayersWithProbability playersWithProbability) {
        playersWithProbability.forEach((player, probability) -> {
            int start = endValue() + 1;
            values.add(new RouletteRange(start, start + probability.value(), player));
        });
    }

    public Player pickPlayer(int number) {
        return values.stream().filter(rouletteRange -> rouletteRange.isBetween(number)).findFirst()
                .orElseThrow(() -> new IllegalStateException("범위에 해당하지 않는 숫자입니다."))
                .player();
    }

    public int endValue() {
        if (values.isEmpty()) {
            return 0;
        }
        return values.getLast().end();
    }
}
