package coffeeshout.room.domain.roulette;

import static org.springframework.util.Assert.isTrue;

import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.room.domain.player.Player;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RankNormalization {

    private final Map<Integer, List<Player>> rankedGroups;

    private RankNormalization(Map<Integer, List<Player>> rankedGroups) {
        this.rankedGroups = Map.copyOf(rankedGroups);
    }

    public static RankNormalization from(MiniGameResult miniGameResult) {
        final Map<Player, Integer> originalRanks = miniGameResult.getRank();
        final Map<Integer, List<Player>> groupedByRank = groupPlayersByRank(originalRanks);
        final Map<Integer, List<Player>> normalizedGroups = normalizeGroups(groupedByRank);
        return new RankNormalization(normalizedGroups);
    }

    public int getNormalizedRank(Player player) {
        for (Map.Entry<Integer, List<Player>> entry : rankedGroups.entrySet()) {
            if (entry.getValue().contains(player)) {
                return entry.getKey();
            }
        }
        throw new IllegalArgumentException("플레이어를 찾을 수 없습니다: " + player);
    }

    public int getTieSize(int rank) {
        List<Player> players = rankedGroups.get(rank);
        isTrue(players != null, "해당 랭크 그룹이 존재하지 않습니다: " + rank);

        return players.size();
    }

    public int getMaxRank() {
        return rankedGroups.keySet().stream()
                .max(Integer::compareTo)
                .orElseThrow(() -> new IllegalStateException("랭크 그룹이 비어 있습니다."));
    }

    private static Map<Integer, List<Player>> groupPlayersByRank(Map<Player, Integer> originalRanks) {
        return originalRanks.entrySet().stream()
                .collect(Collectors.groupingBy(
                        Map.Entry::getValue,
                        Collectors.mapping(Map.Entry::getKey, Collectors.toList())
                ));
    }

    private static Map<Integer, List<Player>> normalizeGroups(Map<Integer, List<Player>> groupedByRank) {
        final List<Integer> sortedRanks = groupedByRank.keySet().stream()
                .sorted()
                .toList();

        final Map<Integer, List<Player>> normalizedGroups = new HashMap<>();
        for (int i = 0; i < sortedRanks.size(); i++) {
            final Integer originalRank = sortedRanks.get(i);
            final Integer normalizedRank = i + 1;
            normalizedGroups.put(normalizedRank, groupedByRank.get(originalRank));
        }

        return normalizedGroups;
    }
}
