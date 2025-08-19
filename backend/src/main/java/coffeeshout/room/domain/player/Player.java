package coffeeshout.room.domain.player;

import java.util.Objects;
import lombok.Getter;

@Getter
public class Player {

    private final PlayerName name;
    private PlayerType playerType;
    private Menu menu;
    private Boolean isReady;
    private Integer colorIndex;

    private Player(PlayerName name, Menu menu, Boolean isReady, PlayerType playerType) {
        this.name = name;
        this.menu = menu;
        this.isReady = isReady;
        this.playerType = playerType;
    }

    public static Player createHost(PlayerName name, Menu menu) {
        return new Player(name, menu, true, PlayerType.HOST);
    }

    public static Player createGuest(PlayerName name, Menu menu) {
        return new Player(name, menu, false, PlayerType.GUEST);
    }

    public static Player createHost(PlayerName name, Menu menu, Integer colorIndex) {
        return new Player(name, menu, true, PlayerType.HOST);
    }

    public static Player createGuest(PlayerName name, Menu menu, Integer colorIndex) {
        return new Player(name, menu, false, PlayerType.GUEST);
    }

    public void selectMenu(Menu menu) {
        this.menu = menu;
    }

    public boolean sameName(PlayerName playerName) {
        return Objects.equals(name, playerName);
    }

    public void assignColorIndex(int colorIndex) {
        this.colorIndex = colorIndex;
    }

    public void updateReadyState(Boolean isReady) {
        this.isReady = isReady;
    }

    public void promote() {
        this.playerType = PlayerType.HOST;
        this.isReady = true;
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
