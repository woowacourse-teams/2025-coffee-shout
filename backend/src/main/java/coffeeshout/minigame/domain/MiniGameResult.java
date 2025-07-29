package coffeeshout.minigame.domain;

import coffeeshout.minigame.domain.cardgame.CardGameScore;
import coffeeshout.room.domain.player.Player;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class MiniGameResult {

    private final Map<Player, Integer> rank;

    public MiniGameResult() {
        this.rank = new HashMap<>();
    }

    public MiniGameResult(Map<Player, Integer> rank) {
        this.rank = rank;
    }

    public Integer getPlayerRank(Player player) {
        return rank.get(player);
    }

    public static MiniGameResult from(@NonNull Map<Player, MiniGameScore> playerScores) {
        List<MiniGameScore> sortedScores = playerScores.values().stream()
                .sorted(Comparator.reverseOrder())
                .toList();
        Map<MiniGameScore, Integer> ranks = calculateRank(sortedScores);
        return new MiniGameResult(playerScores.entrySet().stream().collect(Collectors.toMap(
                Entry::getKey,
                entry -> ranks.get(entry.getValue())
        )));
    }

    private static Map<MiniGameScore, Integer> calculateRank(List<MiniGameScore> sortedScores) {
        Map<MiniGameScore, Integer> ranks = new HashMap<>();
        int rank = 1;
        int count = 0;
        MiniGameScore prevScore = CardGameScore.INF;
        for (MiniGameScore score : sortedScores) {
            count++;
            if (!isTieScore(score, prevScore)) {
                rank = count;
                prevScore = score;
            }
            ranks.put(score, rank);
        }
        return ranks;
    }

    private static boolean isTieScore(MiniGameScore score, MiniGameScore prevScore) {
        return score.equals(prevScore);
    }
}
