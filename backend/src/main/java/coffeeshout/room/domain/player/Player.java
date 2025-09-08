package coffeeshout.room.domain.player;

import coffeeshout.room.domain.menu.OrderMenu;
import java.util.Objects;
import lombok.Getter;

@Getter
public class Player {

    private final PlayerName name;
    private PlayerType playerType;
    private OrderMenu orderMenu;
    private Boolean isReady;
    private Integer colorIndex;

    private Player(PlayerName name, OrderMenu orderMenu, Boolean isReady, PlayerType playerType) {
        this.name = name;
        this.playerType = playerType;
        this.orderMenu = orderMenu;
        this.isReady = isReady;
    }

    public static Player createHost(PlayerName name, OrderMenu orderMenu) {
        return new Player(name, orderMenu, true, PlayerType.HOST);
    }

    public static Player createGuest(PlayerName name, OrderMenu orderMenu) {
        return new Player(name, orderMenu, false, PlayerType.GUEST);
    }

    public void selectMenu(OrderMenu orderMenu) {
        this.orderMenu = orderMenu;
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
