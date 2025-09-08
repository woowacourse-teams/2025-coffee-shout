package coffeeshout.room.domain.player;

import coffeeshout.room.domain.menu.SelectedMenu;
import java.util.Objects;
import lombok.Getter;

@Getter
public class Player {

    private final PlayerName name;
    private PlayerType playerType;
    private SelectedMenu selectedMenu;
    private Boolean isReady;
    private Integer colorIndex;

    private Player(PlayerName name, SelectedMenu selectedMenu, Boolean isReady, PlayerType playerType) {
        this.name = name;
        this.playerType = playerType;
        this.selectedMenu = selectedMenu;
        this.isReady = isReady;
    }

    public static Player createHost(PlayerName name, SelectedMenu selectedMenu) {
        return new Player(name, selectedMenu, true, PlayerType.HOST);
    }

    public static Player createGuest(PlayerName name, SelectedMenu selectedMenu) {
        return new Player(name, selectedMenu, false, PlayerType.GUEST);
    }

    public void selectMenu(SelectedMenu selectedMenu) {
        this.selectedMenu = selectedMenu;
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
