package coffeeshout.coffeeshout.domain.roulette;

import coffeeshout.coffeeshout.domain.player.Player;
import coffeeshout.coffeeshout.domain.player.PlayersWithProbability;

public class Roulette {

    private final RandomGenerator randomGenerator;

    public Roulette(RandomGenerator randomGenerator) {
        this.randomGenerator = randomGenerator;
    }

    public Player spin(PlayersWithProbability playersWithProbability) {
        RouletteRanges rouletteRanges = new RouletteRanges(playersWithProbability);
        int randomNumber = randomGenerator.nextInt(1, rouletteRanges.endValue());
        return rouletteRanges.pickPlayer(randomNumber);
    }
}
