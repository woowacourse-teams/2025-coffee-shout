package coffeeshout.coffeeshout.domain.player;

import coffeeshout.coffeeshout.domain.Menu;
import coffeeshout.coffeeshout.domain.Room;

public class Guest extends Player {

    public Guest(final Long id, final String name, final Menu menu, final Room room) {
        super(id, name, menu, room);
    }
}
