package coffeeshout.room.infra.messaging.handler;

import coffeeshout.global.redis.BaseEvent;
import coffeeshout.global.redis.EventHandler;
import coffeeshout.global.ui.WebSocketResponse;
import coffeeshout.global.websocket.LoggingSimpMessagingTemplate;
import coffeeshout.room.application.RoomService;
import coffeeshout.room.domain.event.PlayerKickEvent;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.ui.response.PlayerResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlayerKickEventHandler implements EventHandler {

    private final LoggingSimpMessagingTemplate messagingTemplate;
    private final RoomService roomService;

    @Override
    public void handle(BaseEvent event) {
        final PlayerKickEvent playerKickEvent = (PlayerKickEvent) event;
        roomService.removePlayer(playerKickEvent.joinCode(), playerKickEvent.playerName());

        final List<Player> players = roomService.getPlayersInternal(playerKickEvent.joinCode());
        final List<PlayerResponse> responses = players.stream()
                .map(PlayerResponse::from)
                .toList();

        messagingTemplate.convertAndSend(
                "/topic/room/" + playerKickEvent.joinCode(),
                WebSocketResponse.success(responses)
        );
    }

    @Override
    public Class<?> eventType() {
        return PlayerKickEvent.class;
    }
}
