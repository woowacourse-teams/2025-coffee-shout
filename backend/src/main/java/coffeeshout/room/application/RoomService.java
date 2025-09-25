package coffeeshout.room.application;

import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.minigame.domain.MiniGameScore;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Playable;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.event.RoomCreateEvent;
import coffeeshout.room.domain.event.RoomJoinEvent;
import coffeeshout.room.domain.menu.Menu;
import coffeeshout.room.domain.menu.MenuTemperature;
import coffeeshout.room.domain.menu.SelectedMenu;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.player.PlayerType;
import coffeeshout.room.domain.player.Winner;
import coffeeshout.room.domain.roulette.Roulette;
import coffeeshout.room.domain.roulette.RoulettePicker;
import coffeeshout.room.domain.service.JoinCodeGenerator;
import coffeeshout.room.domain.service.MenuCommandService;
import coffeeshout.room.domain.service.MenuQueryService;
import coffeeshout.room.domain.service.RoomCommandService;
import coffeeshout.room.domain.service.RoomQueryService;
import coffeeshout.room.infra.RoomEventPublisher;
import coffeeshout.room.infra.messaging.RoomEnterStreamProducer;
import coffeeshout.room.infra.messaging.RoomEventWaitManager;
import coffeeshout.room.ui.request.SelectedMenuRequest;
import coffeeshout.room.ui.response.ProbabilityResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoomService {

    private final RoomQueryService roomQueryService;
    private final RoomCommandService roomCommandService;
    private final MenuQueryService menuQueryService;
    private final QrCodeService qrCodeService;
    private final JoinCodeGenerator joinCodeGenerator;
    private final DelayedRoomRemovalService delayedRoomRemovalService;
    private final RoomEventPublisher roomEventPublisher;
    private final RoomEventWaitManager roomEventWaitManager;
    private final MenuCommandService menuCommandService;
    private final RoomEnterStreamProducer roomEnterStreamProducer;

    @Value("${room.event.timeout}")
    private int eventTimeoutSeconds;

    // === 비동기 메서드들 (REST Controller용) ===

    public CompletableFuture<Room> createRoomAsync(String hostName, SelectedMenuRequest selectedMenuRequest) {
        final JoinCode joinCode = joinCodeGenerator.generate();
        final RoomCreateEvent event = RoomCreateEvent.create(hostName, selectedMenuRequest, joinCode.getValue());

        return processEventAsync(
                event.getEventId(),
                () -> roomEventPublisher.publishEvent(event),
                "방 생성",
                String.format("joinCode=%s", joinCode.getValue()),
                room -> String.format("joinCode=%s", room.getJoinCode().getValue())
        );
    }

    public CompletableFuture<Room> enterRoomAsync(
            String joinCode,
            String guestName,
            SelectedMenuRequest selectedMenuRequest
    ) {
        final RoomJoinEvent event = RoomJoinEvent.create(joinCode, guestName, selectedMenuRequest);

        return processEventAsync(
                event.eventId(),
                () -> roomEnterStreamProducer.broadcastEnterRoom(event),
                "방 참가",
                String.format("joinCode=%s, guestName=%s", joinCode, guestName),
                room -> String.format("joinCode=%s, guestName=%s", joinCode, guestName)
        );
    }

    private <T> CompletableFuture<T> processEventAsync(
            String eventId,
            Runnable eventPublisher,
            String operationName,
            String logParams,
            Function<T, String> successLogParams
    ) {
        final CompletableFuture<T> future = roomEventWaitManager.registerWait(eventId);
        eventPublisher.run();

        return future.orTimeout(eventTimeoutSeconds, TimeUnit.SECONDS)
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        log.error("{} 비동기 처리 실패: eventId={}, {}",
                                operationName, eventId, logParams, throwable);
                        return;
                    }
                    log.info("{} 비동기 처리 완료: {}, eventId={}",
                            operationName, successLogParams.apply(result), eventId);
                });
    }

    // === 기존 동기 메서드들 (테스트용 + 하위 호환성) ===

    public List<Player> changePlayerReadyState(String joinCode, String playerName, Boolean isReady) {
        return changePlayerReadyStateInternal(joinCode, playerName, isReady);
    }

    public Winner spinRoulette(String joinCode, String hostName) {
        final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));
        Player host = room.findPlayer(new PlayerName(hostName));

        return room.spinRoulette(host, new Roulette(new RoulettePicker()));
    }

    public Room getRoomByJoinCode(String joinCode) {
        return roomQueryService.getByJoinCode(new JoinCode(joinCode));
    }

    public List<Player> getPlayersInternal(String joinCode) {
        final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));
        return room.getPlayers();
    }

    public Room createRoomInternal(String hostName, SelectedMenuRequest selectedMenuRequest, String joinCodeValue) {
        final JoinCode joinCode = new JoinCode(joinCodeValue);
        final Menu menu = menuCommandService.convertMenu(selectedMenuRequest.id(), selectedMenuRequest.customName());
        final Room room = Room.createNewRoom(
                joinCode,
                new PlayerName(hostName),
                new SelectedMenu(menu, selectedMenuRequest.temperature())
        );
        assignQrCodeUrl(room);
        scheduleRemoveRoom(joinCode);

        return roomCommandService.save(room);
    }

    public Room enterRoomInternal(String joinCode, String guestName, SelectedMenuRequest selectedMenuRequest) {
        final Menu menu = menuCommandService.convertMenu(selectedMenuRequest.id(), selectedMenuRequest.customName());
        final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));

        room.joinGuest(new PlayerName(guestName), new SelectedMenu(menu, selectedMenuRequest.temperature()));

        return roomCommandService.save(room);
    }


    private void assignQrCodeUrl(Room room) {
        final String qrCodeUrl = qrCodeService.getQrCodeUrl(room.getJoinCode().getValue());
        room.assignQrCodeUrl(qrCodeUrl);
    }

    public Room enterRoom(String joinCode, String guestName, SelectedMenuRequest selectedMenuRequest) {
        return roomCommandService.joinGuest(joinCode, guestName, selectedMenuRequest);
    }

    public List<Player> changePlayerReadyStateInternal(String joinCode, String playerName, Boolean isReady) {
        final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));
        final Player player = room.findPlayer(new PlayerName(playerName));

        if (player.getPlayerType() == PlayerType.HOST) {
            return room.getPlayers();
        }

        player.updateReadyState(isReady);
        roomCommandService.save(room);
        return room.getPlayers();
    }

    public List<MiniGameType> updateMiniGamesInternal(String joinCode, String hostName,
                                                      List<MiniGameType> miniGameTypes) {
        final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));
        room.clearMiniGames();

        miniGameTypes.forEach(miniGameType -> {
            final Playable miniGame = miniGameType.createMiniGame();
            room.addMiniGame(new PlayerName(hostName), miniGame);
        });

        roomCommandService.save(room);

        return room.getAllMiniGame().stream()
                .map(Playable::getMiniGameType)
                .toList();
    }

    // === 나머지 기존 메서드들 (변경 없음) ===

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

    public List<MiniGameType> updateMiniGames(String joinCode, String hostName, List<MiniGameType> miniGameTypes) {
        return updateMiniGamesInternal(joinCode, hostName, miniGameTypes);
    }

    public List<MiniGameType> getAllMiniGames() {
        return Arrays.stream(MiniGameType.values())
                .toList();
    }

    public boolean roomExists(String joinCode) {
        return roomQueryService.existsByJoinCode(new JoinCode(joinCode));
    }

    public boolean isGuestNameDuplicated(String joinCode, String guestName) {
        final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));

        return room.hasDuplicatePlayerName(new PlayerName(guestName));
    }

    public List<ProbabilityResponse> getProbabilities(String joinCode) {
        final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));
        return room.getPlayers().stream()
                .map(ProbabilityResponse::from)
                .toList();
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

    public boolean isReadyState(String joinCode) {
        final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));
        room.showRoulette();
        return room.isReadyState();
    }
}
