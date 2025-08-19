package coffeeshout.room.ui;

import coffeeshout.generator.MessageResponse;
import coffeeshout.generator.Operation;
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

    @MessageResponse(
            value = "/room/{joinCode}",
            returnType = PlayerResponse.class
    )
    @Operation(
            summery = "summery",
            description = "description"
    )
    @MessageMapping("/room/{joinCode}/update-players")
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
            value = "/room/{joinCode}",
            returnType = PlayerResponse.class
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
            value = "/room/{joinCode}",
            returnType = PlayerResponse.class
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
            value = "/room/{joinCode}/roulette",
            returnType = ProbabilityResponse.class
    )
    @Operation(
            summery = "summery",
            description = "description"
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
            value = "/topic/room/{joinCode}/minigame",
            returnType = MiniGameSelectMessage.class
    )
    @Operation(
            summery = "summery",
            description = "description"
    )
    public void broadcastMiniGames(@DestinationVariable String joinCode, MiniGameSelectMessage message) {
        final List<MiniGameType> responses = roomService.updateMiniGames(joinCode, message.hostName(),
                message.miniGameTypes());

        messagingTemplate.convertAndSend("/topic/room/" + joinCode + "/minigame",
                WebSocketResponse.success(responses));
    }

    @MessageMapping("/room/{joinCode}/spin-roulette")
    @MessageResponse(
            value = "/topic/room/{joinCode}/winner",
            returnType = WinnerResponse.class
    )
    @Operation(
            summery = "summery",
            description = "description"
    )
    public void broadcastRouletteSpin(@DestinationVariable String joinCode, RouletteSpinMessage message) {
        final WinnerResponse winner = WinnerResponse.from(roomService.spinRoulette(joinCode, message.hostName()));

        messagingTemplate.convertAndSend("/topic/room/" + joinCode + "/winner",
                WebSocketResponse.success(winner));
    }
}
