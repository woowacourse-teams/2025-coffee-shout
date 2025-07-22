package coffeeshout.room.ui;

import coffeeshout.room.application.RoomService;
import coffeeshout.room.ui.request.MenuChangeMessage;
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

    @MessageMapping("/room/{roomId}/players")
    public void getPlayers(@DestinationVariable Long roomId) {
        final List<PlayerResponse> responses = roomService.getAllPlayers(roomId).stream()
                .map(PlayerResponse::from)
                .toList();

        messagingTemplate.convertAndSend("/topic/room/" + roomId, responses);
    }

    @MessageMapping("/room/{roomId}/menus")
    public void getMenus(@DestinationVariable Long roomId, MenuChangeMessage message) {
        final List<PlayerResponse> responses = roomService.selectMenu(roomId, message.playerName(), message.menuId())
                .stream()
                .map(PlayerResponse::from)
                .toList();

        messagingTemplate.convertAndSend("/topic/room/" + roomId, responses);
    }

    @MessageMapping("/room/{roomId}/probabilities")
    public void getProbabilities(@DestinationVariable Long roomId) {
        final List<ProbabilityResponse> responses = roomService.getProbabilities(roomId).entrySet()
                .stream()
                .map(ProbabilityResponse::from)
                .toList();

        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/roulette", responses);
    }

    @MessageMapping("/room/{roomId}/minigames/")
    public void selectMinigames(@DestinationVariable Long roomId, ) {

    }
}
