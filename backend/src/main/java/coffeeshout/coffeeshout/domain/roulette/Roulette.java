package coffeeshout.coffeeshout.domain.roulette;

import coffeeshout.coffeeshout.domain.player.Player;
import coffeeshout.coffeeshout.domain.player.Players;

public class Roulette {

    private final RandomGenerator randomGenerator;

    public Roulette(RandomGenerator randomGenerator) {
        this.randomGenerator = randomGenerator;
    }

    public Player spin(Players players) {
        RouletteRanges rouletteRanges = new RouletteRanges(players);
        int randomNumber = randomGenerator.nextInt(1, rouletteRanges.endValue());
        return rouletteRanges.pickPlayer(randomNumber);
    }
}
