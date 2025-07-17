package coffeeshout.domain;

public record RouletteRange(int start, int end, Player player) {

    public boolean isBetween(int number) {
        return number >= start && number <= end;
    }
}
