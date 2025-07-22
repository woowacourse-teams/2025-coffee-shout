package coffeeshout.room.domain;

import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.state;

import coffeeshout.room.domain.player.Menu;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.roulette.Probability;
import coffeeshout.room.domain.roulette.Roulette;
import coffeeshout.room.domain.roulette.RoulettePicker;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Player> players;

    @Transient
    private Roulette roulette;

    @Transient
    private List<Playable> miniGames;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "room_state")
    private RoomState roomState;

    private Room(JoinCode joinCode, PlayerName hostName, Menu menu) {
        this.joinCode = joinCode;
        this.host = new Player(hostName, menu);
        this.players = new ArrayList<>();
        this.roomState = RoomState.READY;
        this.miniGames = new ArrayList<>();
        this.roulette = new Roulette(new RoulettePicker());

        join(host);
    }

    public static Room createNewRoom(JoinCode joinCode, PlayerName hostName, Menu menu) {
        return new Room(joinCode, hostName, menu);
    }

    public void joinGuest(PlayerName guestName, Menu menu) {
        state(roomState == RoomState.READY, "READY 상태에서만 참여 가능합니다.");
        state(canJoin(), "방에는 최대 9명만 입장가능합니다.");
        isTrue(checkName(guestName), "중복된 닉네임은 들어올 수 없습니다.");
        join(new Player(guestName, menu));
    }

    public void addMiniGame(PlayerName hostName, Playable miniGame) {
        isTrue(host.sameName(hostName), "호스트가 아닙니다.");
        state(miniGames.size() <= 5, "미니게임은 5개 이하여야 합니다.");
        miniGames.add(miniGame);
    }

    public void removeMiniGame(PlayerName hostName, Playable miniGame) {
        isTrue(host.sameName(hostName), "호스트가 아닙니다.");
        isTrue(miniGames.stream().anyMatch(m -> m.getMiniGameType() == miniGame.getMiniGameType()), "미니게임이 존재하지 않습니다.");
        miniGames.removeIf(m -> m.getMiniGameType() == miniGame.getMiniGameType());
    }

    // TODO 미니게임 결과 반영
//    public void applyMiniGameResult(MiniGameResult miniGameResult) {
//        playersWithProbability.adjustProbabilities(miniGameResult,
//                new ProbabilityCalculator(playersWithProbability.getPlayerCount(), miniGames.size()));
//    }

    public boolean hasNoMiniGames() {
        return miniGames.isEmpty();
    }

    public boolean isPlayingState() {
        return roomState == RoomState.PLAYING;
    }

    public Player startRoulette() {
        state(hasEnoughPlayers(), "룰렛은 2~9명의 플레이어가 참여해야 시작할 수 있습니다.");
        state(isPlayingState(), "게임 중일때만 룰렛을 돌릴 수 있습니다.");
        final Player losePlayer = roulette.spin();
        roomState = RoomState.DONE;
        return losePlayer;
    }

    public boolean isHost(Player player) {
        return host.equals(player);
    }

    public Player findPlayer(PlayerName playerName) {
        return players.stream()
                .filter(player -> player.getName().equals(playerName)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("플레이어가 존재하지 않습니다."));
    }

//    TODO: 미니게임 플레이 어떻게 할까
//    public void playMiniGame(int miniGameIndex) {
//       MiniGameResult result = miniGames.get(miniGameIndex).play();
//       this.roomState = RoomState.PLAYING;
//       applyMiniGameResult(result);
    //    }
    // 이벤트 수신하는 메서드

    private void join(Player player) {
        player.assignRoom(this);  // 양방향 연관관계 설정
        players.add(player);
        roulette.join(player);
    }

    public Map<Player, Probability> getProbabilities() {
        return roulette.getProbabilities();
    }

    public List<Playable> getAllMiniGame() {
        return Collections.unmodifiableList(miniGames);
    }

    private boolean hasEnoughPlayers() {
        return players.size() >= MINIMUM_GUEST_COUNT && players.size() <= MAXIMUM_GUEST_COUNT;
    }

    private boolean canJoin() {
        return players.size() < MAXIMUM_GUEST_COUNT;
    }

    private boolean checkName(PlayerName guestName) {
        return players.stream().noneMatch(player -> player.getName().equals(guestName));
    }

    public void load() {
        this.roulette = new Roulette(new RoulettePicker());
        for (final Player player : players) {
            roulette.join(player);
        }
    }
}
