package coffeeshout.room.application;

import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.minigame.domain.MiniGameScore;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Playable;
import coffeeshout.room.domain.Room;
import java.util.concurrent.CompletableFuture;
import coffeeshout.room.domain.event.RoomCreateEvent;
import coffeeshout.room.domain.menu.CustomMenu;
import coffeeshout.room.domain.menu.Menu;
import coffeeshout.room.domain.menu.MenuTemperature;
import coffeeshout.room.domain.menu.SelectedMenu;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.player.PlayerType;
import coffeeshout.room.domain.player.Winner;
import coffeeshout.room.domain.roulette.Probability;
import coffeeshout.room.domain.service.JoinCodeGenerator;
import coffeeshout.room.domain.service.MenuQueryService;
import coffeeshout.room.domain.service.RoomCommandService;
import coffeeshout.room.domain.service.RoomQueryService;
import coffeeshout.room.infra.RoomEventPublisher;
import coffeeshout.room.infra.RoomCreationWaitManager;
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
    private final JoinCodeGenerator joinCodeGenerator;
    private final DelayedRoomRemovalService delayedRoomRemovalService;
    private final RoomEventPublisher roomEventPublisher;
    private final RoomCreationWaitManager roomCreationWaitManager;
    private final String defaultCategoryImage;

    public RoomService(
            RoomQueryService roomQueryService,
            RoomCommandService roomCommandService,
            MenuQueryService menuQueryService,
            QrCodeService qrCodeService,
            JoinCodeGenerator joinCodeGenerator,
            DelayedRoomRemovalService delayedRoomRemovalService,
            RoomEventPublisher roomEventPublisher,
            RoomCreationWaitManager roomCreationWaitManager,
            @Value("${menu-category.default-image}") String defaultCategoryImage
    ) {
        this.roomQueryService = roomQueryService;
        this.roomCommandService = roomCommandService;
        this.menuQueryService = menuQueryService;
        this.qrCodeService = qrCodeService;
        this.joinCodeGenerator = joinCodeGenerator;
        this.delayedRoomRemovalService = delayedRoomRemovalService;
        this.roomEventPublisher = roomEventPublisher;
        this.roomCreationWaitManager = roomCreationWaitManager;
        this.defaultCategoryImage = defaultCategoryImage;
    }

    public Room createRoom(String hostName, SelectedMenuRequest selectedMenuRequest) {
        // joinCode 먼저 생성
        final JoinCode joinCode = joinCodeGenerator.generate();
        
        // 이벤트 발행만 함 - 실제 생성은 리스너에서
        final RoomCreateEvent event = RoomCreateEvent.create(hostName, selectedMenuRequest, joinCode.getValue());
        
        // Future 등록
        roomCreationWaitManager.registerWait(event.getEventId());
        
        // 이벤트 발행
        roomEventPublisher.publishRoomCreateEvent(event);
        
        // Future로 결과 대기 (최대 5초)
        final Room room = roomCreationWaitManager.waitForCompletion(event.getEventId(), 5);
        
        if (room == null) {
            log.error("방 생성 이벤트 처리 실패: eventId={}, joinCode={}", event.getEventId(), joinCode.getValue());
            throw new RuntimeException("방 생성 실패");
        }
        
        log.info("방 생성 완료: joinCode={}, eventId={}", room.getJoinCode().getValue(), event.getEventId());
        return room;
    }
    
    public Room createRoomInternal(String hostName, SelectedMenuRequest selectedMenuRequest, String joinCode) {
        return createRoomInternal(hostName, selectedMenuRequest, new JoinCode(joinCode));
    }
    
    public Room createRoomInternal(String hostName, SelectedMenuRequest selectedMenuRequest, JoinCode joinCode) {
        final Menu menu = convertMenu(selectedMenuRequest);
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

        return room.getPlayers();
    }

    public List<Player> changePlayerReadyState(String joinCode, String playerName, Boolean isReady) {
        final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));
        final Player player = room.findPlayer(new PlayerName(playerName));

        if (player.getPlayerType() != PlayerType.HOST) {
            player.updateReadyState(isReady);
        }

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
            final Playable miniGame = miniGameType.createMiniGame();
            room.addMiniGame(new PlayerName(hostName), miniGame);
        });

        return room.getAllMiniGame().stream()
                .map(Playable::getMiniGameType)
                .toList();
    }

    public boolean roomExists(String joinCode) {
        return roomQueryService.existsByJoinCode(new JoinCode(joinCode));
    }

    public Winner spinRoulette(String joinCode, String hostName) {
        final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));
        final Player host = room.findPlayer(new PlayerName(hostName));

        return room.spinRoulette(host);
    }

    public boolean isGuestNameDuplicated(String joinCode, String guestName) {
        final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));

        return room.hasDuplicatePlayerName(new PlayerName(guestName));
    }

    public Map<Player, MiniGameScore> getMiniGameScores(String joinCode, MiniGameType miniGameType) {
        final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));
        final Playable miniGame = room.findMiniGame(miniGameType);

        return miniGame.getScores();
    }

    public MiniGameResult getMiniGameRanks(String joinCode, MiniGameType miniGameType) {
        final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));
        final Playable miniGame = room.findMiniGame(miniGameType);

        return miniGame.getResult();
    }

    public List<MiniGameType> getSelectedMiniGames(String joinCode) {
        final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));
        return room.getSelectedMiniGameTypes();
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

    public boolean isReadyState(String joinCode) {
        final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));
        return room.isReadyState();
    }
}
