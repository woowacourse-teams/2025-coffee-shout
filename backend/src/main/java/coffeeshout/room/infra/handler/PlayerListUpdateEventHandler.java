package coffeeshout.room.infra.handler;

import coffeeshout.global.ui.WebSocketResponse;
import coffeeshout.global.websocket.LoggingSimpMessagingTemplate;
import coffeeshout.room.application.RoomService;
import coffeeshout.room.domain.event.PlayerListUpdateEvent;
import coffeeshout.room.domain.event.RoomEventType;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.ui.response.PlayerResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlayerListUpdateEventHandler implements RoomEventHandler<PlayerListUpdateEvent> {

    private final RoomService roomService;
    private final LoggingSimpMessagingTemplate messagingTemplate;

    @Override
    public void handle(PlayerListUpdateEvent event) {
        try {
            log.info("플레이어 목록 업데이트 이벤트 수신: eventId={}, joinCode={}",
                    event.getEventId(), event.getJoinCode());

            final List<Player> players = roomService.getPlayersInternal(event.getJoinCode());
            final List<PlayerResponse> responses = players.stream()
                    .map(PlayerResponse::from)
                    .toList();

            messagingTemplate.convertAndSend("/topic/room/" + event.getJoinCode(),
                    WebSocketResponse.success(responses));

            log.info("플레이어 목록 업데이트 이벤트 처리 완료: eventId={}, joinCode={}",
                    event.getEventId(), event.getJoinCode());

        } catch (Exception e) {
            log.error("플레이어 목록 업데이트 이벤트 처리 실패", e);
        }
    }

    @Override
    public RoomEventType getSupportedEventType() {
        return RoomEventType.PLAYER_LIST_UPDATE;
    }
}
