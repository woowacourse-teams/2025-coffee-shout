package coffeeshout.coffeeshout.domain.player;

import coffeeshout.coffeeshout.domain.Menu;
import coffeeshout.coffeeshout.domain.Room;
import java.util.Objects;

public class Player {

    private Long id;

    private String name;

    private Menu menu;

    protected Room room;

    public Player(Long id, String name, Menu menu, Room room) {
        this.id = id;
        this.name = name;
        this.menu = menu;
        this.room = room;
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
        return Objects.equals(id, player.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
