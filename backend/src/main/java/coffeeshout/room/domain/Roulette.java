package coffeeshout.room.domain;

import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.player.domain.Player;

public class Roulette {

    private final RouletteProbabilities rouletteProbabilities;
    private final RandomGenerator randomGenerator;

    public Roulette(RandomGenerator randomGenerator) {
        this.randomGenerator = randomGenerator;
        rouletteProbabilities = new RouletteProbabilities();
    }

    public void join(Player player) {
        rouletteProbabilities.add(player);
    }

    public Player spin() {
        final RouletteRanges rouletteRanges = new RouletteRanges(rouletteProbabilities);
        final int randomNumber = randomGenerator.nextInt(1, rouletteRanges.endValue());
        return rouletteRanges.pickPlayer(randomNumber);
    }

    public void adjustProbabilities(MiniGameResult miniGameResult, int roundCount) {
        rouletteProbabilities.adjustProbabilities(miniGameResult, roundCount);
    }
}
