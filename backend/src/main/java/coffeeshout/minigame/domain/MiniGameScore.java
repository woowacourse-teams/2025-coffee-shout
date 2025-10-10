package coffeeshout.minigame.domain;

import java.util.Objects;

public abstract class MiniGameScore implements Comparable<MiniGameScore> {

    @Override
    public int hashCode() {
        return Objects.hash(getValue());
    }

    @Override
    public int compareTo(final MiniGameScore o) {
        return Long.compare(this.getValue(), o.getValue());
    }

    public abstract long getValue();
}
