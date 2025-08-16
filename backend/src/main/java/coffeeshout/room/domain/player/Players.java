package coffeeshout.room.domain.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import lombok.Getter;

@Getter
public class Players {

    private static final Random RANDOM = new Random();

    private final List<Player> players;
    private final ColorUsage colorUsage;

    public Players() {
        this.players = Collections.synchronizedList(new ArrayList<>());
        this.colorUsage = new ColorUsage();
    }

    public Player join(Player player) {
        player.assignColorIndex(colorUsage.pickRandomOne());
        this.players.add(player);
        return getPlayer(player.getName());
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

    public boolean hasDuplicateName(PlayerName playerName) {
        return players.stream().anyMatch(player -> player.sameName(playerName));
    }

    public boolean isAllReady() {
        return players.stream()
                .allMatch(Player::getIsReady);
    }

    public boolean removePlayer(PlayerName playerName) {
        return players.removeIf(player -> {
            if (player.sameName(playerName)) {
                colorUsage.release(player.getColorIndex());
                return true;
            }
            return false;
        });
    }

    public boolean existsByName(PlayerName playerName) {
        return players.stream()
                .anyMatch(player -> player.sameName(playerName));
    }

    public Player getRandomPlayer() {
        return players.get(RANDOM.nextInt(players.size()));
    }
}
