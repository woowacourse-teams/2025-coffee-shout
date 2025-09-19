package coffeeshout.room.application;

import coffeeshout.minigame.application.MiniGameServiceManager;
import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.menu.CustomMenu;
import coffeeshout.room.domain.menu.Menu;
import coffeeshout.room.domain.menu.MenuTemperature;
import coffeeshout.room.domain.menu.SelectedMenu;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.player.PlayerType;
import coffeeshout.room.domain.player.Winner;
import coffeeshout.room.domain.roulette.Probability;
import coffeeshout.room.domain.roulette.Roulette;
import coffeeshout.room.domain.roulette.RoulettePicker;
import coffeeshout.room.domain.service.JoinCodeGenerator;
import coffeeshout.room.domain.service.MenuQueryService;
import coffeeshout.room.domain.service.RoomCommandService;
import coffeeshout.room.domain.service.RoomQueryService;
import coffeeshout.room.ui.request.SelectedMenuRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RoomService {

    private final RoomQueryService roomQueryService;
    private final RoomCommandService roomCommandService;
    private final MenuQueryService menuQueryService;
    private final QrCodeService qrCodeService;
    private final MiniGameServiceManager miniGameManager;
    private final JoinCodeGenerator joinCodeGenerator;
    private final DelayedRoomRemovalService delayedRoomRemovalService;
    private final String defaultCategoryImage;

    public RoomService(
            RoomQueryService roomQueryService,
            RoomCommandService roomCommandService,
            MenuQueryService menuQueryService,
            QrCodeService qrCodeService,
            MiniGameServiceManager miniGameManager,
            JoinCodeGenerator joinCodeGenerator,
            DelayedRoomRemovalService delayedRoomRemovalService,
            @Value("${menu-category.default-image}") String defaultCategoryImage
    ) {
        this.roomQueryService = roomQueryService;
        this.roomCommandService = roomCommandService;
        this.menuQueryService = menuQueryService;
        this.qrCodeService = qrCodeService;
        this.miniGameManager = miniGameManager;
        this.joinCodeGenerator = joinCodeGenerator;
        this.delayedRoomRemovalService = delayedRoomRemovalService;
        this.defaultCategoryImage = defaultCategoryImage;
    }

    public Room createRoom(String hostName, SelectedMenuRequest selectedMenuRequest) {
        final Menu menu = convertMenu(selectedMenuRequest);
        final JoinCode joinCode = joinCodeGenerator.generate();
        final Room room = Room.createNewRoom(
                joinCode,
                new PlayerName(hostName),
                new SelectedMenu(menu, selectedMenuRequest.temperature())
        );
        final String qrCodeUrl = qrCodeService.getQrCodeUrl(room.getJoinCode().getValue());
        room.assignQrCodeUrl(qrCodeUrl);
        scheduleRemoveRoom(joinCode);

        return roomCommandService.save(room);
    }

    public Room enterRoom(String joinCode, String guestName, SelectedMenuRequest selectedMenuRequest) {
        final Menu menu = convertMenu(selectedMenuRequest);
        final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));

        room.joinGuest(new PlayerName(guestName), new SelectedMenu(menu, selectedMenuRequest.temperature()));

        return roomCommandService.save(room);
    }

    public List<Player> getAllPlayers(String joinCode) {
        final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));

        return room.getPlayers();
    }

    public List<Player> selectMenu(String joinCode, String playerName, Long menuId) {
        final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));
        final Menu menu = menuQueryService.getById(menuId);

        final Player player = room.findPlayer(new PlayerName(playerName));
        player.selectMenu(new SelectedMenu(menu, MenuTemperature.ICE));

        roomCommandService.save(room);

        return room.getPlayers();
    }

    public List<Player> changePlayerReadyState(String joinCode, String playerName, Boolean isReady) {
        final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));
        final Player player = room.findPlayer(new PlayerName(playerName));

        if (player.getPlayerType() != PlayerType.HOST) {
            player.updateReadyState(isReady);
        }

        roomCommandService.save(room);

        return room.getPlayers();
    }

    public Map<Player, Probability> getProbabilities(String joinCode) {
        final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));

        return room.getProbabilities();
    }

    public List<MiniGameType> getAllMiniGames() {
        return Arrays.stream(MiniGameType.values())
                .toList();
    }

    public List<MiniGameType> updateMiniGames(String joinCode, String hostName, List<MiniGameType> miniGameTypes) {
        final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));
        room.clearMiniGames();

        miniGameTypes.forEach(miniGameType -> {
            room.addMiniGame(new PlayerName(hostName), miniGameType);
        });

        roomCommandService.save(room);

        return room.getAllMiniGames();
    }

    public boolean roomExists(String joinCode) {
        return roomQueryService.existsByJoinCode(new JoinCode(joinCode));
    }

    public Winner spinRoulette(String joinCode, String hostName) {
        final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));
        final Player host = room.findPlayer(new PlayerName(hostName));
        final Winner winner = room.spinRoulette(host, new Roulette(new RoulettePicker()));

        roomCommandService.save(room);
        return winner;
    }

    public boolean isGuestNameDuplicated(String joinCode, String guestName) {
        final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));

        return room.hasDuplicatePlayerName(new PlayerName(guestName));
    }

    public List<MiniGameType> getSelectedMiniGames(String joinCode) {
        final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));
        return room.getAllMiniGames();
    }

    public boolean removePlayer(String joinCode, String playerName) {
        final JoinCode code = new JoinCode(joinCode);
        final Room room = roomQueryService.getByJoinCode(code);
        final boolean isRemoved = room.removePlayer(new PlayerName(playerName));
        if (room.isEmpty()) {
            roomCommandService.delete(code);
        }

        return isRemoved;
    }

    public void applyMiniGameResult(String joinCode, MiniGameResult miniGameResult) {
        final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));
        room.applyMiniGameResult(miniGameResult);
        roomCommandService.save(room);
    }

    public boolean isReadyState(String joinCode) {
        final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));
        return room.isReadyState();
    }

    public void startNextGame(String joinCode, String hostName) {
        final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));
        MiniGameType miniGameType = room.startNextGame(hostName);
        miniGameManager.startGame(miniGameType, joinCode, room.getPlayers());
        roomCommandService.save(room);
    }

    private void scheduleRemoveRoom(JoinCode joinCode) {
        try {
            delayedRoomRemovalService.scheduleRemoveRoom(joinCode);
        } catch (Exception e) {
            log.error("방 제거 스케줄링 실패: joinCode={}", joinCode.getValue(), e);
        }
    }

    private Menu convertMenu(SelectedMenuRequest selectedMenuRequest) {
        if (selectedMenuRequest.id() == 0) {
            return new CustomMenu(selectedMenuRequest.customName(), defaultCategoryImage);
        }
        return menuQueryService.getById(selectedMenuRequest.id());
    }
}
