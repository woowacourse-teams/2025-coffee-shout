package coffeeshout.room.application;

import coffeeshout.global.config.InstanceConfig;
import coffeeshout.global.redis.RedisMessagePublisher;
import coffeeshout.global.redis.event.minigame.MiniGameStartedEvent;
import coffeeshout.global.redis.event.minigame.MiniGamesUpdatedEvent;
import coffeeshout.global.redis.event.player.HostPromotedEvent;
import coffeeshout.global.redis.event.player.PlayerJoinedEvent;
import coffeeshout.global.redis.event.player.PlayerMenuSelectedEvent;
import coffeeshout.global.redis.event.player.PlayerReadyStateChangedEvent;
import coffeeshout.global.redis.event.player.PlayerRemovedEvent;
import coffeeshout.global.redis.event.roulette.RouletteSpunEvent;
import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.minigame.domain.MiniGameScore;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Playable;
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
    private final JoinCodeGenerator joinCodeGenerator;
    private final DelayedRoomRemovalService delayedRoomRemovalService;
    private final RedisMessagePublisher messagePublisher;
    private final InstanceConfig instanceConfig;
    private final String defaultCategoryImage;

    public RoomService(
            RoomQueryService roomQueryService,
            RoomCommandService roomCommandService,
            MenuQueryService menuQueryService,
            QrCodeService qrCodeService,
            JoinCodeGenerator joinCodeGenerator,
            DelayedRoomRemovalService delayedRoomRemovalService,
            RedisMessagePublisher messagePublisher,
            InstanceConfig instanceConfig,
            @Value("${menu-category.default-image}") String defaultCategoryImage
    ) {
        this.roomQueryService = roomQueryService;
        this.roomCommandService = roomCommandService;
        this.menuQueryService = menuQueryService;
        this.qrCodeService = qrCodeService;
        this.joinCodeGenerator = joinCodeGenerator;
        this.delayedRoomRemovalService = delayedRoomRemovalService;
        this.messagePublisher = messagePublisher;
        this.instanceConfig = instanceConfig;
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
        Room saved = roomCommandService.save(room);

        // 플레이어 입장 이벤트 발행
        Player joinedPlayer = saved.findPlayer(new PlayerName(guestName));
        messagePublisher.publishPlayerJoined(new PlayerJoinedEvent(
                joinCode,
                guestName,
                joinedPlayer.getPlayerType(),
                joinedPlayer.getSelectedMenu(),
                joinedPlayer.getIsReady(),
                joinedPlayer.getColorIndex(),
                instanceConfig.getInstanceId()
        ));

        return saved;
    }

    public List<Player> getAllPlayers(String joinCode) {
        final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));

        return room.getPlayers();
    }

    public List<Player> selectMenu(String joinCode, String playerName, Long menuId) {
        final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));
        final Menu menu = menuQueryService.getById(menuId);

        final Player player = room.findPlayer(new PlayerName(playerName));
        SelectedMenu selectedMenu = new SelectedMenu(menu, MenuTemperature.ICE);
        player.selectMenu(selectedMenu);

        roomCommandService.save(room);

        // 플레이어 메뉴 선택 이벤트 발행
        messagePublisher.publishPlayerMenuSelected(new PlayerMenuSelectedEvent(
                joinCode,
                playerName,
                selectedMenu,
                instanceConfig.getInstanceId()
        ));

        return room.getPlayers();
    }

    public List<Player> changePlayerReadyState(String joinCode, String playerName, Boolean isReady) {
        final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));
        final Player player = room.findPlayer(new PlayerName(playerName));

        if (player.getPlayerType() != PlayerType.HOST) {
            player.updateReadyState(isReady);
            roomCommandService.save(room);

            // 플레이어 준비 상태 변경 이벤트 발행
            messagePublisher.publishPlayerReadyStateChanged(new PlayerReadyStateChangedEvent(
                    joinCode,
                    playerName,
                    isReady,
                    instanceConfig.getInstanceId()
            ));
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

        roomCommandService.save(room);

        // 미니게임 목록 업데이트 이벤트 발행
        messagePublisher.publishMiniGamesUpdated(new MiniGamesUpdatedEvent(
                joinCode,
                miniGameTypes,
                instanceConfig.getInstanceId()
        ));

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

        Winner winner = room.spinRoulette(host);
        roomCommandService.save(room);

        // 룰렛 스핀 이벤트 발행
        messagePublisher.publishRouletteSpun(new RouletteSpunEvent(
                joinCode,
                winner,
                instanceConfig.getInstanceId()
        ));

        return winner;
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
        
        // 제거되는 플레이어가 호스트인지 확인
        final Player removingPlayer = room.findPlayer(new PlayerName(playerName));
        final boolean wasHost = room.isHost(removingPlayer);
        
        final boolean isRemoved = room.removePlayer(new PlayerName(playerName));
        
        if (isRemoved) {
            // 호스트가 나갔고 방이 비어있지 않으면 새로운 호스트가 승격됨
            String newHostName = null;
            if (wasHost && !room.isEmpty()) {
                newHostName = room.getHost().getName().value();
            }
            
            if (room.isEmpty()) {
                roomCommandService.delete(code);
            } else {
                roomCommandService.save(room);
            }

            // 플레이어 제거 이벤트 발행
            messagePublisher.publishPlayerRemoved(new PlayerRemovedEvent(
                    joinCode,
                    playerName,
                    instanceConfig.getInstanceId()
            ));

            // 호스트 승격 이벤트 발행
            if (newHostName != null) {
                messagePublisher.publishHostPromoted(new HostPromotedEvent(
                        joinCode,
                        newHostName,
                        instanceConfig.getInstanceId()
                ));
            }
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

    public Playable startMiniGame(String joinCode, String hostName) {
        final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));
        final Playable currentGame = room.startNextGame(hostName);

        roomCommandService.save(room);

        // 미니게임 시작 이벤트 발행
        messagePublisher.publishMiniGameStarted(new MiniGameStartedEvent(
                joinCode,
                currentGame.getMiniGameType(),
                room.getPlayers().stream().map(p -> p.getName().value()).toList(),
                instanceConfig.getInstanceId()
        ));

        return currentGame;
    }
}
