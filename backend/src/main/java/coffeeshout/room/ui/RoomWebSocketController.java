package coffeeshout.room.ui;

import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.room.application.RoomService;
import coffeeshout.room.infra.BroadcastEventPublisher;
import coffeeshout.room.ui.event.ErrorBroadcastEvent;
import coffeeshout.room.ui.event.MiniGameUpdateBroadcastEvent;
import coffeeshout.room.ui.event.PlayerUpdateBroadcastEvent;
import coffeeshout.room.ui.event.ProbabilityUpdateBroadcastEvent;
import coffeeshout.room.ui.event.WinnerAnnouncementBroadcastEvent;
import coffeeshout.room.ui.request.MenuChangeMessage;
import coffeeshout.room.ui.request.MiniGameSelectMessage;
import coffeeshout.room.ui.request.ReadyChangeMessage;
import coffeeshout.room.ui.request.RouletteSpinMessage;
import coffeeshout.room.ui.response.PlayerResponse;
import coffeeshout.room.ui.response.ProbabilityResponse;
import coffeeshout.room.ui.response.WinnerResponse;
import generator.annotaions.MessageResponse;
import generator.annotaions.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class RoomWebSocketController {

    private final BroadcastEventPublisher broadcastEventPublisher;
    private final RoomService roomService;

    @MessageMapping("/room/{joinCode}/update-players")
    @MessageResponse(
            path = "/room/{joinCode}",
            returnType = List.class,
            genericType = PlayerResponse.class
    )
    @Operation(
            summary = "플레이어 목록 업데이트 및 브로드캐스트",
            description = """
                    방의 플레이어 목록을 업데이트하고 모든 참가자에게 브로드캐스트합니다.
                    방에 참가한 모든 플레이어들의 정보를 조회하여 실시간으로 공유합니다.
                    """
    )
    public void broadcastPlayers(@DestinationVariable String joinCode) {
        final List<PlayerResponse> responses = roomService.getAllPlayers(joinCode)
                .stream()
                .map(PlayerResponse::from)
                .toList();

        final PlayerUpdateBroadcastEvent event = PlayerUpdateBroadcastEvent.create(joinCode, responses);
        broadcastEventPublisher.publishPlayerUpdateEvent(event);
    }

    @MessageMapping("/room/{joinCode}/update-menus")
    @MessageResponse(
            path = "/room/{joinCode}",
            returnType = List.class,
            genericType = PlayerResponse.class
    )
    @Operation(
            summary = "플레이어 메뉴 선택 업데이트 및 브로드캐스트",
            description = """
                    플레이어의 메뉴 선택을 업데이트하고 변경된 플레이어 목록을 브로드캐스트합니다.
                    특정 플레이어가 메뉴를 선택하면 해당 정보를 저장하고 모든 참가자에게 업데이트된 상태를 전달합니다.
                    """
    )
    public void broadcastMenus(@DestinationVariable String joinCode, MenuChangeMessage message) {
        final List<PlayerResponse> responses = roomService.selectMenu(joinCode, message.playerName(),
                        message.menuId())
                .stream()
                .map(PlayerResponse::from)
                .toList();

        final PlayerUpdateBroadcastEvent event = PlayerUpdateBroadcastEvent.create(joinCode, responses);
        broadcastEventPublisher.publishPlayerUpdateEvent(event);
    }

    @MessageMapping("/room/{joinCode}/update-ready")
    @MessageResponse(
            path = "/room/{joinCode}",
            returnType = List.class,
            genericType = PlayerResponse.class
    )
    @Operation(
            summary = "플레이어 준비 상태 변경 및 브로드캐스트",
            description = """
                    플레이어의 준비 상태를 변경하고 업데이트된 플레이어 목록을 브로드캐스트합니다.
                    플레이어가 게임 준비 완료 또는 준비 취소를 할 때 해당 상태를 저장하고
                    모든 참가자에게 변경된 준비 상태를 실시간으로 전달합니다.
                    """
    )
    public void broadcastReady(@DestinationVariable String joinCode, ReadyChangeMessage message) {
        roomService.changePlayerReadyStateAsync(joinCode, message.playerName(), message.isReady())
                .thenAccept(players -> {
                    final List<PlayerResponse> responses = players.stream()
                            .map(PlayerResponse::from)
                            .toList();
                    final PlayerUpdateBroadcastEvent event = PlayerUpdateBroadcastEvent.create(joinCode, responses);
                    broadcastEventPublisher.publishPlayerUpdateEvent(event);
                })
                .exceptionally(throwable -> {
                    final ErrorBroadcastEvent errorEvent = ErrorBroadcastEvent.create(
                            joinCode,
                            "ready 상태 변경 실패: " + throwable.getMessage(),
                            "/topic/room/" + joinCode + "/error"
                    );
                    broadcastEventPublisher.publishErrorEvent(errorEvent);
                    return null;
                });
    }

    @MessageMapping("/room/{joinCode}/get-probabilities")
    @MessageResponse(
            path = "/room/{joinCode}/roulette",
            returnType = List.class,
            genericType = ProbabilityResponse.class
    )
    @Operation(
            summary = "룰렛 확률 정보 조회 및 브로드캐스트",
            description = """
                    룰렛 게임의 확률 정보를 조회하고 모든 참가자에게 브로드캐스트합니다.
                    각 플레이어별 당첨 확률을 계산하여 룰렛 채널을 통해 실시간으로 전달합니다.
                    """
    )
    public void broadcastProbabilities(@DestinationVariable String joinCode) {
        final List<ProbabilityResponse> responses = roomService.getProbabilities(joinCode).entrySet()
                .stream()
                .map(ProbabilityResponse::from)
                .toList();

        final ProbabilityUpdateBroadcastEvent event = ProbabilityUpdateBroadcastEvent.create(joinCode, responses);
        broadcastEventPublisher.publishProbabilityUpdateEvent(event);
    }

    @MessageMapping("/room/{joinCode}/update-minigames")
    @MessageResponse(
            path = "/room/{joinCode}/minigame",
            returnType = List.class,
            genericType = MiniGameType.class
    )
    @Operation(
            summary = "미니게임 목록 업데이트 및 브로드캐스트",
            description = """
                    호스트가 선택한 미니게임 목록을 업데이트하고 모든 참가자에게 브로드캐스트합니다.
                    방장이 게임에서 사용할 미니게임들을 선택하면 해당 정보를 저장하고
                    미니게임 채널을 통해 선택된 게임 타입들을 실시간으로 전달합니다.
                    """
    )
    public void broadcastMiniGames(@DestinationVariable String joinCode, MiniGameSelectMessage message) {
        roomService.updateMiniGamesAsync(joinCode, message.hostName(), message.miniGameTypes())
                .thenAccept(selectedMiniGames -> {
                    final MiniGameUpdateBroadcastEvent event = MiniGameUpdateBroadcastEvent.create(joinCode,
                            selectedMiniGames);
                    broadcastEventPublisher.publishMiniGameUpdateEvent(event);
                })
                .exceptionally(throwable -> {
                    final ErrorBroadcastEvent errorEvent = ErrorBroadcastEvent.create(
                            joinCode,
                            "미니게임 선택 실패: " + throwable.getMessage(),
                            "/topic/room/" + joinCode + "/error"
                    );
                    broadcastEventPublisher.publishErrorEvent(errorEvent);
                    return null;
                });
    }

    @MessageMapping("/room/{joinCode}/spin-roulette")
    @MessageResponse(
            path = "/room/{joinCode}/winner",
            returnType = WinnerResponse.class
    )
    @Operation(
            summary = "룰렛 게임 실행 및 당첨자 발표",
            description = """
                    호스트가 룰렛을 돌려서 당첨자를 결정하고 결과를 모든 참가자에게 브로드캐스트합니다.
                    """
    )
    public void broadcastRouletteSpin(@DestinationVariable String joinCode, RouletteSpinMessage message) {
        roomService.spinRouletteAsync(joinCode, message.hostName())
                .thenAccept(winner -> {
                    final WinnerResponse response = WinnerResponse.from(winner);
                    final WinnerAnnouncementBroadcastEvent event = WinnerAnnouncementBroadcastEvent.create(joinCode,
                            response);
                    broadcastEventPublisher.publishWinnerAnnouncementEvent(event);
                })
                .exceptionally(throwable -> {
                    final ErrorBroadcastEvent errorEvent = ErrorBroadcastEvent.create(
                            joinCode,
                            "룰렛 스핀 실패: " + throwable.getMessage(),
                            "/topic/room/" + joinCode + "/error"
                    );
                    broadcastEventPublisher.publishErrorEvent(errorEvent);
                    return null;
                });
    }
}
