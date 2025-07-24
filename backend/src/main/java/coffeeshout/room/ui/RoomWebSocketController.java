package coffeeshout.room.ui;

import coffeeshout.global.ui.WebSocketResponse;
import coffeeshout.room.application.RoomService;
import coffeeshout.room.domain.MiniGameType;
import coffeeshout.room.ui.request.MenuChangeMessage;
import coffeeshout.room.ui.request.MiniGameSelectMessage;
import coffeeshout.room.ui.response.PlayerResponse;
import coffeeshout.room.ui.response.ProbabilityResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class RoomWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final RoomService roomService;

    @MessageMapping("/room/{joinCode}/players")
    public void broadcastPlayers(@DestinationVariable String joinCode) {
        try {
            final List<PlayerResponse> responses = roomService.getAllPlayers(joinCode).stream()
                    .map(PlayerResponse::from)
                    .toList();

            messagingTemplate.convertAndSend("/topic/room/" + joinCode,
                    WebSocketResponse.success(responses));
        } catch (Exception e) {
            messagingTemplate.convertAndSend("/topic/room/" + joinCode,
                    WebSocketResponse.error("플레이어 정보를 불러올 수 없습니다."));

        }
    }

    @MessageMapping("/room/{joinCode}/menus")
    public void broadcastMenus(@DestinationVariable String joinCode, MenuChangeMessage message) {
        try {
            final List<PlayerResponse> responses = roomService.selectMenu(joinCode, message.playerName(),
                            message.menuId())
                    .stream()
                    .map(PlayerResponse::from)
                    .toList();

            messagingTemplate.convertAndSend("/topic/room/" + joinCode,
                    WebSocketResponse.success(responses));
        } catch (Exception e) {
            messagingTemplate.convertAndSend("/topic/room/" + joinCode,
                    WebSocketResponse.error("메뉴 정보를 불러올 수 없습니다."));
        }
    }

    @MessageMapping("/room/{joinCode}/probabilities")
    public void broadcastProbabilities(@DestinationVariable String joinCode) {
        try {
            final List<ProbabilityResponse> responses = roomService.getProbabilities(joinCode).entrySet()
                    .stream()
                    .map(ProbabilityResponse::from)
                    .toList();

            messagingTemplate.convertAndSend("/topic/room/" + joinCode + "/roulette",
                    WebSocketResponse.success(responses));
        } catch (Exception e) {
            messagingTemplate.convertAndSend("/topic/room/" + joinCode + "/roulette",
                    WebSocketResponse.error("확률 정보를 불러올 수 없습니다."));
        }
    }

    @MessageMapping("/room/{joinCode}/minigames/select")
    public void broadcastSelectedMinigames(@DestinationVariable String joinCode, MiniGameSelectMessage message) {
        try {
            final List<MiniGameType> responses = roomService.selectMiniGame(joinCode, message.hostName(),
                    message.miniGameType());

            messagingTemplate.convertAndSend("/topic/room/" + joinCode + "/minigame",
                    WebSocketResponse.success(responses));
        } catch (Exception e) {
            messagingTemplate.convertAndSend("/topic/room/" + joinCode + "/minigame",
                    WebSocketResponse.error("선택된 게임 정보를 불러올 수 없습니다."));
        }
    }

    @MessageMapping("/room/{joinCode}/minigames/unselect")
    public void broadcastUnselectedMinigames(@DestinationVariable String joinCode, MiniGameSelectMessage message) {
        try {
            final List<MiniGameType> responses = roomService.unselectMiniGame(joinCode, message.hostName(),
                    message.miniGameType());

            messagingTemplate.convertAndSend("/topic/room/" + joinCode + "/minigame",
                    WebSocketResponse.success(responses));
        } catch (Exception e) {
            messagingTemplate.convertAndSend("/topic/room/" + joinCode + "/minigame",
                    WebSocketResponse.error("선택 해제된 게임 정보를 불러올 수 없습니다."));
        }

    }

}
