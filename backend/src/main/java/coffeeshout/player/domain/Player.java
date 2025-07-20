package coffeeshout.player.domain;

import coffeeshout.room.domain.Probability;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Player {

    private final String name;
    private Menu menu;

    @Setter
    private Probability probability;

    public Player(String name, Menu menu) {
        this.name = name;
        this.menu = menu;
        this.probability = new Probability(0);
    }

    public void selectMenu(Menu menu) {
        this.menu = menu;
    }

    public boolean sameName(String name) {
        return this.name.equals(name);
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Player player)) {
            return false;
        }
        return Objects.equals(name, player.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
