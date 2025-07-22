package coffeeshout.room.ui;

import coffeeshout.room.application.RoomService;
import coffeeshout.room.ui.request.MenuChangeMessage;
import coffeeshout.room.ui.response.PlayerResponse;
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

    @MessageMapping("/room/{roomId}/menu")
    public void getMenu(
            @DestinationVariable Long roomId,
            MenuChangeMessage message
    ) {
        final List<PlayerResponse> responses = roomService.selectMenu(roomId, message.playerName(), message.menuId())
                .stream()
                .map(PlayerResponse::from)
                .toList();

        messagingTemplate.convertAndSend("/topic/room/" + roomId, responses);
    }
}
