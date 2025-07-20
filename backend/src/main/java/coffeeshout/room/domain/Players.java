package coffeeshout.room.domain;

import static org.springframework.util.Assert.isTrue;

import coffeeshout.player.domain.Player;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Players {

    private static final int MAXIMUM_GUEST_COUNT = 9;
    private static final int MINIMUM_GUEST_COUNT = 2;

    private final List<Player> players;

    public Players() {
        this.players = Collections.synchronizedList(new ArrayList<>());
    }

    public void add(Player joinPlayer) {
        isTrue(players.size() < MAXIMUM_GUEST_COUNT, "게임은 최대 9명까지 참여할 수 있습니다.");
        isTrue(!hasPlayerWithName(joinPlayer.getName()), "이미 존재하는 플레이어 이름입니다.");
        players.add(joinPlayer);
    }

    public int getPlayerCount() {
        return players.size();
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public boolean hasEnoughPlayers() {
        return players.size() >= MINIMUM_GUEST_COUNT
                && players.size() <= MAXIMUM_GUEST_COUNT;
    }

    public Player findPlayer(String playerName) {
        return players.stream()
                .filter(player -> player.getName().equals(playerName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("사용지가 존재하지 않습니다."));
    }

    private boolean hasPlayerWithName(String name) {
        return players.stream()
                .anyMatch(player -> player.isSameName(name));
    }
}
