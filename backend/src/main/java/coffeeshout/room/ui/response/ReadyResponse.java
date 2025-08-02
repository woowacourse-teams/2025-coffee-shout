package coffeeshout.room.ui.response;

import coffeeshout.room.domain.player.Player;
import java.util.List;

public record ReadyResponse(
        int totalPlayerCount,
        int readyPlayerCount
) {

    public static ReadyResponse from(List<Player> players) {
        int readyCount = (int) players.stream()
                .filter(Player::getIsReady)
                .count();

        return new ReadyResponse(players.size(), readyCount);
    }
}
