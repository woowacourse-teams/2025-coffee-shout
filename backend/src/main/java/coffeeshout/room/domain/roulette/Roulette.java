package coffeeshout.room.domain.roulette;

import static coffeeshout.room.domain.RoomErrorCode.NO_EXIST_PLAYER;

import coffeeshout.global.exception.custom.InvalidArgumentException;
import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerName;
import java.util.LinkedHashMap;
import java.util.Map;

public class Roulette {

    private final RandomPicker randomPicker;
    private final Map<Player, Probability> playerProbabilities;

    public Roulette(RandomPicker randomGenerator) {
        this.randomPicker = randomGenerator;
        this.playerProbabilities = new LinkedHashMap<>();
    }

    public Player spin() {
        final RouletteRanges rouletteRanges = new RouletteRanges(playerProbabilities);
        final int randomNumber = randomPicker.nextInt(1, rouletteRanges.endValue());
        return rouletteRanges.pickPlayer(randomNumber);
    }

    public void join(Player player) {
        this.playerProbabilities.put(player, Probability.ZERO);
        final Probability probability = Probability.TOTAL.divide(getPlayerCount());
        for (Map.Entry<Player, Probability> entry : playerProbabilities.entrySet()) {
            entry.setValue(probability);
        }
    }

    public void adjustProbabilities(MiniGameResult miniGameResult, ProbabilityCalculator probabilityCalculator) {
        for (Player player : playerProbabilities.keySet()) {
            final int rank = miniGameResult.getPlayerRank(player);
            final int probabilityChange = probabilityCalculator.calculateProbabilityChange(rank,
                    miniGameResult.getTieCountByRank(rank));
            final Probability adjustedProbability = getProbability(player).plus(probabilityChange);
            playerProbabilities.put(player, adjustedProbability);
        }
    }

    public Probability getProbability(Player player) {
        return playerProbabilities.get(player);
    }

    private int getPlayerCount() {
        return playerProbabilities.size();
    }

    public Map<Player, Probability> getProbabilities() {
        return Map.copyOf(playerProbabilities);
    }

    public boolean removePlayer(PlayerName playerName) {
        final Player player = getPlayer(playerName);

        if (playerProbabilities.remove(player) == null) {
            return false;
        }

        // 남은 플레이어들의 확률 재조정
        if (!playerProbabilities.isEmpty()) {
            final Probability probability = Probability.TOTAL.divide(getPlayerCount());
            for (Map.Entry<Player, Probability> entry : playerProbabilities.entrySet()) {
                entry.setValue(probability);
            }
        }
        return true;
    }

    private Player getPlayer(PlayerName playerName) {
        return playerProbabilities.keySet().stream()
                .filter(p -> p.sameName(playerName))
                .findFirst()
                .orElseThrow(() -> new InvalidArgumentException(NO_EXIST_PLAYER, "플레이어가 존재하지 않습니다."));
    }
}
