package coffeeshout.room.domain;

import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.state;

import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.player.domain.Player;
import coffeeshout.room.domain.probability.PlayersWithProbability;
import coffeeshout.room.domain.probability.ProbabilityCalculator;
import coffeeshout.room.domain.range.RouletteRanges;
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
public class RouletteRoom {

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
    private List<Player> players = new ArrayList<>();

    @Transient
    private RandomGenerator randomGenerator;

    @Transient
    private List<Playable> miniGames = new ArrayList<>();

    @Transient
    private List<MiniGameResult> miniGameResults = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "room_state")
    private RoomState roomState;

    public RouletteRoom(JoinCode joinCode, Player host, RandomGenerator randomGenerator) {
        this.joinCode = joinCode;
        this.randomGenerator = randomGenerator;
        this.host = host;
        this.roomState = RoomState.READY;
        players.add(host);
    }

    public void joinGuest(Player joinPlayer) {
        state(players.size() <= 9, "정원 초과(최대인원-9명)");
        state(roomState == RoomState.READY, "READY 상태에서만 참여 가능합니다.");
        boolean existPlayer = players.stream().noneMatch(player -> player.sameName(joinPlayer.getName()));
        isTrue(existPlayer, "이미 존재하는 플레이어입니다.");
        players.add(joinPlayer);
    }

    public void setMiniGame(List<Playable> miniGames) {
        state(!miniGames.isEmpty() && miniGames.size() <= 5, "미니게임은 1~5개 이하여야 합니다.");
        this.miniGames = miniGames;
    }

    public PlayersWithProbability getProbabilities() {
        return PlayersWithProbability.probability(players, miniGameResults, new ProbabilityCalculator(players.size(), totalRound()));
    }

    public void addMiniGameResult(MiniGameResult miniGameResult) {
        miniGameResults.add(miniGameResult);
    }

    public Player spin() {
        state(hasEnoughPlayers(), "룰렛은 2~9명의 플레이어가 참여해야 시작할 수 있습니다.");
        state(isInPlayingState(), "게임 중일때만 룰렛을 돌릴 수 있습니다.");
        final RouletteRanges rouletteRanges = new RouletteRanges(getProbabilities());
        final int randomNumber = randomGenerator.nextInt(1, rouletteRanges.endValue());
        Player winner = rouletteRanges.pickPlayer(randomNumber);
        roomState = RoomState.DONE;
        return winner;
    }

    public boolean isHost(Player player) {
        return host.equals(player);
    }

    public Player findPlayer(String playerName) {
        return players.stream()
                .filter(player -> player.sameName(playerName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 플레이어입니다."));
    }

    private int totalRound() {
        return miniGames.size();
    }

    private boolean isInPlayingState() {
        return roomState == RoomState.PLAYING;
    }

    private boolean hasEnoughPlayers() {
        return players.size() >= MINIMUM_GUEST_COUNT && players.size() <= MAXIMUM_GUEST_COUNT;
    }
}
