package coffeeshout.room.domain;

import static org.springframework.util.Assert.state;

import coffeeshout.minigame.domain.MiniGame;
import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.player.domain.Menu;
import coffeeshout.player.domain.Player;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@NaturalIdCache
@Getter
public class Room {

    private static final int MAXIMUM_GUEST_COUNT = 9;
    private static final int MINIMUM_GUEST_COUNT = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NaturalId
    @Embedded
    private JoinCode joinCode;

    @Transient
    private Player host;

    @Transient
    private List<Player> players;

    @Transient
    private RandomGenerator randomGenerator;

    @Transient
    private List<MiniGame> miniGames;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "room_state")
    private RoomState roomState;

    public Room(JoinCode joinCode, String hostName, Menu menu) {
        this.joinCode = joinCode;
        this.randomGenerator = new JavaRandomGenerator();
        this.host = new Player(hostName, menu);
        this.players = List.of(host);
        this.roomState = RoomState.READY;
        this.miniGames = new ArrayList<>();
    }

    public static Room createNewRoom(JoinCode joinCode, String hostName, Menu menu) {
        final Room room = new Room(joinCode, hostName, menu);
        room.adjustProbability();
        return room;
    }

    public void joinGuest(String guestName, Menu menu) {
        state(roomState == RoomState.READY, "READY 상태에서만 참여 가능합니다.");
        players.add(new Player(guestName, menu));
        adjustProbability();
    }

    private void adjustProbability() {
        players.forEach(player -> player.setProbability(new Probability(10000 / players.size())));
    }

    public void setMiniGame(List<MiniGame> miniGames) {
        state(miniGames.size() <= 5, "미니게임은 5개 이하여야 합니다.");
        this.miniGames = miniGames;
    }

    // TODO 미니게임 결과 반영
    public void applyMiniGameResult(MiniGameResult miniGameResult) {
        playersWithProbability.adjustProbabilities(miniGameResult,
                new ProbabilityCalculator(playersWithProbability.getPlayerCount(), miniGames.size()));
    }

    public boolean hasNoMiniGames() {
        return miniGames.isEmpty();
    }

    public boolean isInPlayingState() {
        return roomState == RoomState.PLAYING;
    }

    public boolean hasEnoughPlayers() {
        return playersWithProbability.getPlayerCount() >= MINIMUM_GUEST_COUNT
                && playersWithProbability.getPlayerCount() <= MAXIMUM_GUEST_COUNT;
    }

    public Player startRoulette() {
        state(hasEnoughPlayers(), "룰렛은 2~9명의 플레이어가 참여해야 시작할 수 있습니다.");
        state(isInPlayingState(), "게임 중일때만 룰렛을 돌릴 수 있습니다.");
        final Player losePlayer = roulette.spin(playersWithProbability);
        roomState = RoomState.DONE;
        return losePlayer;
    }

    public boolean isHost(Player player) {
        return host.equals(player);
    }

    public List<Player> getPlayers() {
        return playersWithProbability.getPlayers();
    }

    public Player findPlayer(String playerName) {
        return playersWithProbability.getPlayer(playerName);
    }

//    TODO: 미니게임 플레이 어떻게 할까
//    public void playMiniGame(int miniGameIndex) {
//       MiniGameResult result = miniGames.get(miniGameIndex).play();
//       this.roomState = RoomState.PLAYING;
//       applyMiniGameResult(result);
//    }

    // 이벤트 수신하는 메서드
}
