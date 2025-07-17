package coffeeshout.domain;

public class Roulette {

    private final RandomGenerator randomGenerator;

    public Roulette(RandomGenerator randomGenerator) {
        this.randomGenerator = randomGenerator;
    }

    public Player spin(PlayersWithProbability playersWithProbability) {
        final RouletteRanges rouletteRanges = new RouletteRanges(playersWithProbability);
        final int randomNumber = randomGenerator.nextInt(1, rouletteRanges.endValue());
        return rouletteRanges.pickPlayer(randomNumber);
    }
}
