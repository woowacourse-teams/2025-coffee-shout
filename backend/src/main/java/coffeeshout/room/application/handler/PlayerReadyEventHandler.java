package coffeeshout.room.application.handler;

import coffeeshout.global.ui.WebSocketResponse;
import coffeeshout.global.websocket.LoggingSimpMessagingTemplate;
import coffeeshout.room.application.RoomEventHandler;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.event.PlayerReadyEvent;
import coffeeshout.room.domain.event.RoomEventType;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.player.PlayerType;
import coffeeshout.room.domain.service.RoomCommandService;
import coffeeshout.room.domain.service.RoomQueryService;
import coffeeshout.room.ui.response.PlayerResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlayerReadyEventHandler implements RoomEventHandler<PlayerReadyEvent> {

    private final RoomCommandService roomCommandService;
    private final RoomQueryService roomQueryService;
    private final LoggingSimpMessagingTemplate messagingTemplate;

    @Override
    public void handle(PlayerReadyEvent event) {
        try {
            log.info("플레이어 ready 이벤트 수신: eventId={}, joinCode={}, playerName={}, isReady={}",
                    event.eventId(), event.joinCode(), event.playerName(), event.isReady());

            final Room room = roomQueryService.getByJoinCode(new JoinCode(event.joinCode()));
            final Player player = room.findPlayer(new PlayerName(event.playerName()));

            if (player.getPlayerType() != PlayerType.HOST) {
                player.updateReadyState(event.isReady());
                roomCommandService.save(room);
            }

            final List<PlayerResponse> responses = room.getPlayers().stream()
                    .map(PlayerResponse::from)
                    .toList();

            messagingTemplate.convertAndSend("/topic/room/" + event.joinCode(),
                    WebSocketResponse.success(responses));

            log.info("플레이어 ready 이벤트 처리 완료: eventId={}, joinCode={}, playerName={}, isReady={}",
                    event.eventId(), event.joinCode(), event.playerName(), event.isReady());

        } catch (Exception e) {
            log.error("플레이어 ready 이벤트 처리 실패", e);
        }
    }

    @Override
    public RoomEventType getSupportedEventType() {
        return RoomEventType.PLAYER_READY;
    }
}
