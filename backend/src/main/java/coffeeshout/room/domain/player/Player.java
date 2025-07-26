package coffeeshout.room.domain.player;

import java.util.Objects;
import lombok.Getter;

@Getter
public class Player {

    private final PlayerName name;
    private Menu menu;

    public Player(PlayerName name) {
        this.name = name;
    }

    public Player(PlayerName name, Menu menu) {
        this.name = name;
        this.menu = menu;
    }

    public void selectMenu(Menu menu) {
        this.menu = menu;
    }

    public boolean sameName(PlayerName playerName) {
        return Objects.equals(name, playerName);
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
