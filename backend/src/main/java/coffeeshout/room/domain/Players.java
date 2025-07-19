package coffeeshout.room.domain;

import coffeeshout.player.domain.Player;
import java.util.List;

public class Players {

    private static final int MAXIMUM_GUEST_COUNT = 9;
    private static final int MINIMUM_GUEST_COUNT = 2;

    private final PlayerInfos playerInfos;

    public Players(PlayerInfos playerInfos) {
        this.playerInfos = playerInfos;
    }

    public void join(Player player) {
        playerInfos.join(player);
    }

    public boolean hasEnoughPlayers() {
        return playerInfos.getPlayerCount() >= MINIMUM_GUEST_COUNT
                && playerInfos.getPlayerCount() <= MAXIMUM_GUEST_COUNT;
    }

    public List<Player> getPlayers() {
        return playerInfos.getPlayers();
    }

    public Player getPlayer(String playerName) {
        return playerInfos.getPlayer(playerName);
    }
}
