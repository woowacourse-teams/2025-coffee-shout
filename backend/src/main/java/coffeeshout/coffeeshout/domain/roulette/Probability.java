package coffeeshout.coffeeshout.domain.roulette;

public record Probability(Integer value) {

    public Probability divide(int number) {
        return new Probability(value / number);
    }

    public Probability minus(Probability other) {
        return new Probability(this.value - other.value);
    }

    public Probability plus(Probability other) {
        return new Probability(this.value + other.value);
    }

    public Probability invert(){
        return new Probability(-value);
    }


}
