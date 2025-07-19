package coffeeshout.room.domain;

import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.player.domain.Player;

public class Roulette {

    private final PlayerInfos playerInfos;
    private final RandomGenerator randomGenerator;

    public Roulette(PlayerInfos playerInfos, RandomGenerator randomGenerator) {
        this.playerInfos = playerInfos;
        this.randomGenerator = randomGenerator;
    }

    public Player spin() {
        final RouletteRanges rouletteRanges = new RouletteRanges(playerInfos);
        final int randomNumber = randomGenerator.nextInt(1, rouletteRanges.endValue());
        return rouletteRanges.pickPlayer(randomNumber);
    }

    public void adjustProbabilities(MiniGameResult miniGameResult, int round) {
        playerInfos.adjustProbabilities(miniGameResult, new ProbabilityCalculator(playerInfos.getPlayerCount(), round));
    }
}
