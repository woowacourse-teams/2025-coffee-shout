package coffeeshout.room.ui;

import coffeeshout.global.ui.WebSocketResponse;
import coffeeshout.global.websocket.LoggingSimpMessagingTemplate;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.room.application.RoomService;
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

    private final LoggingSimpMessagingTemplate messagingTemplate;
    private final RoomService roomService;

    @MessageMapping("/room/{joinCode}/update-players")
    @MessageResponse(
            path = "/topic/room/{joinCode}",
            returnType = PlayerResponse.class
    )
    @Operation(
            description = "description",
            summary = "summary"
    )
    public void broadcastPlayers(@DestinationVariable String joinCode) {
        final List<PlayerResponse> responses = roomService.getAllPlayers(joinCode)
                .stream()
                .map(PlayerResponse::from)
                .toList();

        messagingTemplate.convertAndSend("/topic/room/" + joinCode,
                WebSocketResponse.success(responses));
    }

    @MessageMapping("/room/{joinCode}/update-menus")
    @MessageResponse(
            path = "/topic/room/{joinCode}",
            returnType = PlayerResponse.class
    )
    @Operation(
            description = "description",
            summary = "summary"
    )
    public void broadcastMenus(@DestinationVariable String joinCode, MenuChangeMessage message) {
        final List<PlayerResponse> responses = roomService.selectMenu(joinCode, message.playerName(),
                        message.menuId())
                .stream()
                .map(PlayerResponse::from)
                .toList();

        messagingTemplate.convertAndSend("/topic/room/" + joinCode,
                WebSocketResponse.success(responses));
    }

    @MessageMapping("/room/{joinCode}/update-ready")
    @MessageResponse(
            path = "/topic/room/{joinCode}",
            returnType = PlayerResponse.class
    )
    @Operation(
            description = "description",
            summary = "summary"
    )
    public void broadcastReady(@DestinationVariable String joinCode, ReadyChangeMessage message) {
        final List<PlayerResponse> responses = roomService.changePlayerReadyState(joinCode, message.playerName(),
                        message.isReady())
                .stream()
                .map(PlayerResponse::from)
                .toList();

        messagingTemplate.convertAndSend("/topic/room/" + joinCode,
                WebSocketResponse.success(responses));
    }

    @MessageMapping("/room/{joinCode}/get-probabilities")
    @MessageResponse(
            path = "/topic/room/{joinCode}",
            returnType = ProbabilityResponse.class
    )
    @Operation(
            description = "description",
            summary = "summary"
    )
    public void broadcastProbabilities(@DestinationVariable String joinCode) {
        final List<ProbabilityResponse> responses = roomService.getProbabilities(joinCode).entrySet()
                .stream()
                .map(ProbabilityResponse::from)
                .toList();

        messagingTemplate.convertAndSend("/topic/room/" + joinCode + "/roulette",
                WebSocketResponse.success(responses));
    }

    @MessageMapping("/room/{joinCode}/update-minigames")
    @MessageResponse(
            path = "/topic/room/{joinCode}",
            returnType = MiniGameType.class
    )
    @Operation(
            description = "description",
            summary = "summary"
    )
    public void broadcastMiniGames(@DestinationVariable String joinCode, MiniGameSelectMessage message) {
        final List<MiniGameType> responses = roomService.updateMiniGames(joinCode, message.hostName(),
                message.miniGameTypes());

        messagingTemplate.convertAndSend("/topic/room/" + joinCode + "/minigame",
                WebSocketResponse.success(responses));
    }

    @MessageMapping("/room/{joinCode}/spin-roulette")
    @MessageResponse(
            path = "/topic/room/{joinCode}",
            returnType = WinnerResponse.class
    )
    @Operation(
            description = "description",
            summary = "summary"
    )
    public void broadcastRouletteSpin(@DestinationVariable String joinCode, RouletteSpinMessage message) {
        final WinnerResponse winner = WinnerResponse.from(roomService.spinRoulette(joinCode, message.hostName()));

        messagingTemplate.convertAndSend("/topic/room/" + joinCode + "/winner",
                WebSocketResponse.success(winner));
    }
}
