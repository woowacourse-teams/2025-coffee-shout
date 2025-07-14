package coffeeshout.coffeeshout.domain.player;

import coffeeshout.coffeeshout.domain.Menu;
import coffeeshout.coffeeshout.domain.Room;

public abstract class Player {

    private Long id;

    private String name;

    private Menu menu;

    protected Room room;

    public Player(final Long id, final String name, final Menu menu, final Room room) {
        this.id = id;
        this.name = name;
        this.menu = menu;
        this.room = room;
    }

    public boolean isSameName(Player player) {
        return this.name.equals(player.name);
    }
}
