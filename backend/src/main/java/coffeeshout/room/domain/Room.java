package coffeeshout.room.domain;

import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.state;

import coffeeshout.global.exception.custom.InvalidArgumentException;
import coffeeshout.global.exception.custom.InvalidStateException;
import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.room.domain.player.Menu;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.player.Players;
import coffeeshout.room.domain.roulette.Probability;
import coffeeshout.room.domain.roulette.ProbabilityCalculator;
import coffeeshout.room.domain.roulette.Roulette;
import coffeeshout.room.domain.roulette.RoulettePicker;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.Getter;

@Getter
public class Room {

    private static final int MAXIMUM_GUEST_COUNT = 9;
    private static final int MINIMUM_GUEST_COUNT = 2;

    private JoinCode joinCode;
    private Player host;
    private Players players;
    private Roulette roulette;
    private List<Playable> miniGames;
    private RoomState roomState;

    public Room(JoinCode joinCode, PlayerName hostName, Menu menu) {
        this.joinCode = joinCode;
        this.host = Player.createHost(hostName, menu);
        this.players = new Players();
        this.roomState = RoomState.READY;
        this.miniGames = new ArrayList<>();
        this.roulette = new Roulette(new RoulettePicker());

        join(host);
    }

    public static Room createNewRoom(JoinCode joinCode, PlayerName hostName, Menu menu) {
        return new Room(joinCode, hostName, menu);
    }

    public void joinGuest(PlayerName guestName, Menu menu) {
        validateRoomReady();
        validateCanJoin();
        validatePlayerNameNotDuplicate(guestName);
        join(Player.createGuest(guestName, menu));
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

    public void applyMiniGameResult(MiniGameResult miniGameResult) {
        ProbabilityCalculator probabilityCalculator = new ProbabilityCalculator(
                players.getPlayerCount(),
                calculateRoundCount()
        );
        roulette.adjustProbabilities(miniGameResult, probabilityCalculator);
    }

    private int calculateRoundCount() {
        return miniGames.size();
    }

    public boolean hasNoMiniGames() {
        return miniGames.isEmpty();
    }

    public boolean isPlayingState() {
        return roomState == RoomState.PLAYING;
    }

    public Player spinRoulette(Player host) {
        isTrue(isHost(host), "호스트만 룰렛을 돌릴 수 있습니다.");
        state(hasEnoughPlayers(), "룰렛은 2~9명의 플레이어가 참여해야 시작할 수 있습니다.");
        state(isPlayingState(), "게임 중일때만 룰렛을 돌릴 수 있습니다.");
        // TODO 룰렛을 돌리기 전에 모든 게임들을 플레이해야 한다.
        final Player winner = roulette.spin();
        roomState = RoomState.DONE;
        return winner;
    }

    public boolean isHost(Player player) {
        return host.equals(player);
    }

    public List<Player> getPlayers() {
        return players.getPlayers();
    }

    public Player findPlayer(PlayerName playerName) {
        return players.getPlayer(playerName);
    }

    public Playable playMiniGame(Integer gameIndex) {
        final Playable miniGame = miniGames.get(gameIndex);
        miniGame.startGame(players);
        return miniGame;
    }

//    TODO: 미니게임 플레이 어떻게 할까
//    public void playMiniGame(int miniGameIndex) {
//       MiniGameResult result = miniGames.get(miniGameIndex).play();
//       this.roomState = RoomState.PLAYING;
//       applyMiniGameResult(result);
    //    }
    // 이벤트 수신하는 메서드

    private void join(Player player) {
        players.join(player);
        roulette.join(player);
    }

    public Map<Player, Probability> getProbabilities() {
        return roulette.getProbabilities();
    }

    public List<Playable> getAllMiniGame() {
        return Collections.unmodifiableList(miniGames);
    }

    public Playable findMiniGame(coffeeshout.minigame.domain.MiniGameType miniGameType) {
        return miniGames.stream()
                .filter(minigame -> minigame.getMiniGameType() == miniGameType)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당하는 미니게임이 존재하지 않습니다."));
    }

    public void startGame(MiniGameType miniGameType) {
        findMiniGame(miniGameType).startGame(players);
        this.roomState = RoomState.PLAYING;
    }

    private boolean hasEnoughPlayers() {
        return players.hasEnoughPlayers(MINIMUM_GUEST_COUNT, MAXIMUM_GUEST_COUNT);
    }

    private boolean canJoin() {
        return players.getPlayerCount() < MAXIMUM_GUEST_COUNT;
    }

    private boolean checkName(PlayerName guestName) {
        return players.notExistPlayerName(guestName);
    }

    private void validateRoomReady() {
        if (roomState != RoomState.READY) {
            throw new InvalidStateException(RoomErrorCode.ROOM_NOT_READY_TO_JOIN,
                    "READY 상태에서만 참여 가능합니다. 현재 상태: " + roomState);
        }
    }

    private void validateCanJoin() {
        if (!canJoin()) {
            throw new InvalidStateException(RoomErrorCode.ROOM_FULL,
                    "방에는 최대 9명만 입장가능합니다. 현재 인원: " + players.getPlayerCount());
        }
    }

    private void validatePlayerNameNotDuplicate(PlayerName guestName) {
        if (!checkName(guestName)) {
            throw new InvalidArgumentException(RoomErrorCode.DUPLICATE_PLAYER_NAME,
                    "중복된 닉네임은 들어올 수 없습니다. 닉네임: " + guestName.value());
        }
    }

}
