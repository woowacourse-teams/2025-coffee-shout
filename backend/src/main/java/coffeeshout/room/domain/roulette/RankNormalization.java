package coffeeshout.room.domain.roulette;

import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.room.domain.player.Player;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RankNormalization {

    private final Map<Player, Integer> normalizedRanks;

    private RankNormalization(Map<Player, Integer> normalizedRanks) {
        this.normalizedRanks = normalizedRanks;
    }

    public static RankNormalization from(MiniGameResult miniGameResult) {
        final Map<Player, Integer> originalRanks = miniGameResult.getRank();
        final Map<Player, Integer> normalizedRanks = normalizeRanks(originalRanks);
        return new RankNormalization(normalizedRanks);
    }

    public Integer getNormalizedRank(Player player) {
        return normalizedRanks.get(player);
    }

    private static Map<Player, Integer> normalizeRanks(Map<Player, Integer> originalRanks) {
        final List<Integer> distinctRanks = originalRanks.values().stream()
                .distinct()
                .sorted()
                .toList();

        final Map<Integer, Integer> rankMapping = createRankMapping(distinctRanks);

        return originalRanks.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> rankMapping.get(entry.getValue())
                ));
    }

    private static Map<Integer, Integer> createRankMapping(List<Integer> distinctRanks) {
        final Map<Integer, Integer> mapping = new HashMap<>();
        for (int i = 0; i < distinctRanks.size(); i++) {
            mapping.put(distinctRanks.get(i), i + 1);
        }
        return mapping;
    }
}
