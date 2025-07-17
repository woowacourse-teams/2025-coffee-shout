package coffeeshout.coffeeshout.domain;

import java.util.Objects;
import lombok.ToString;

@ToString
public class Player {

    private Long id;

    private String name;

    private Menu menu;

    public Player(String name, Menu menu) {
        this.name = name;
        this.menu = menu;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Player player)) {
            return false;
        }
        return Objects.equals(id, player.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
