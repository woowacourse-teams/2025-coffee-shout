package coffeeshout.coffeeshout.domain.player;

import coffeeshout.coffeeshout.domain.Menu;
import coffeeshout.coffeeshout.domain.Room;

public class Guest extends Player {

    public Guest(Long id, String name, Menu menu, Room room) {
        super(id, name, menu, room);
    }
}
