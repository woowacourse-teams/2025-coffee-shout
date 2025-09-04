package coffeeshout.room.domain.player;

import coffeeshout.room.domain.menu.Menu;
import coffeeshout.room.domain.menu.MenuTemperature;
import java.util.Objects;
import lombok.Getter;

@Getter
public class Player {

    private final PlayerName name;
    private PlayerType playerType;
    private Menu menu;
    private MenuTemperature menuTemperature;
    private Boolean isReady;
    private Integer colorIndex;

    private Player(PlayerName name, Menu menu, Boolean isReady, PlayerType playerType, MenuTemperature menuTemperature) {
        this.name = name;
        this.menu = menu;
        this.isReady = isReady;
        this.playerType = playerType;
        this.menuTemperature = menuTemperature;
    }

    public static Player createHost(PlayerName name, Menu menu) {
        return new Player(name, menu, true, PlayerType.HOST, MenuTemperature.ICE);
    }

    public static Player createHost(PlayerName name, Menu menu, MenuTemperature menuTemperature) {
        return new Player(name, menu, true, PlayerType.HOST, menuTemperature);
    }

    public static Player createGuest(PlayerName name, Menu menu) {
        return new Player(name, menu, false, PlayerType.GUEST, MenuTemperature.ICE);
    }

    public static Player createGuest(PlayerName name, Menu menu, MenuTemperature menuTemperature) {
        return new Player(name, menu, false, PlayerType.GUEST, menuTemperature);
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
