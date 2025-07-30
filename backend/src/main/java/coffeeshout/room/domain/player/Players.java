package coffeeshout.room.domain.player;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class Players {

    private final List<Player> players;

    public Players() {
        this.players = new ArrayList<>();
    }

    public void join(Player player) {
        this.players.add(player);
    }

    public boolean hasEnoughPlayers(int minimumGuestCount, int maximumGuestCount) {
        return players.size() >= minimumGuestCount && players.size() <= maximumGuestCount;
    }

    public Player getPlayer(PlayerName playerName) {
        return players.stream()
                .filter(p -> p.sameName(playerName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));
    }

    public int getPlayerCount() {
        return players.size();
    }

    public boolean hasDuplicateName(PlayerName playerNmae) {
        return players.stream().anyMatch(player -> player.sameName(playerNmae));
    }
}
