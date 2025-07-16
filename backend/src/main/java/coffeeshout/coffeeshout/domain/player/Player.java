package coffeeshout.coffeeshout.domain.player;

import java.util.Objects;
import lombok.ToString;

@ToString
public class Player {

    private String name;

    private Menu menu;

    public Player(String name, Menu menu) {
        this.name = name;
        this.menu = menu;
    }

    public boolean isSameName(Player player) {
        return this.name.equals(player.name);
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
