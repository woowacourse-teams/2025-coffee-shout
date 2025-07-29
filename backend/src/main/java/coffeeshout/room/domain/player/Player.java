package coffeeshout.room.domain.player;

import java.util.Objects;
import lombok.Getter;

@Getter
public class Player {

    private final PlayerName name;
    private Menu menu;
    private final PlayerType playerType;

    private Player(PlayerName name, Menu menu, PlayerType playerType) {
        this.name = name;
        this.menu = menu;
        this.playerType = playerType;
    }

    public static Player createHost(PlayerName name, Menu menu) {
        return new Player(name, menu, PlayerType.HOST);
    }

    public static Player createGuest(PlayerName name, Menu menu) {
        return new Player(name, menu, PlayerType.GUEST);
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
