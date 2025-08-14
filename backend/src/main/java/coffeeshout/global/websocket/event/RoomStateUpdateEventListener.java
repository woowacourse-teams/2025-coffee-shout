package coffeeshout.global.websocket.event;

import coffeeshout.global.ui.WebSocketResponse;
import coffeeshout.global.websocket.LoggingSimpMessagingTemplate;
import coffeeshout.room.application.RoomService;
import coffeeshout.room.ui.response.PlayerResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomStateUpdateEventListener {

    private final RoomService roomService;
    private final LoggingSimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleRoomStateUpdate(RoomStateUpdateEvent event) {
        try {
            log.info("방 상태 업데이트 이벤트 처리: joinCode={}, reason={}", event.getJoinCode(), event.getReason());
            
            final List<PlayerResponse> responses = roomService.getAllPlayers(event.getJoinCode())
                    .stream()
                    .map(PlayerResponse::from)
                    .toList();

            messagingTemplate.convertAndSend("/topic/room/" + event.getJoinCode(),
                    WebSocketResponse.success(responses));
                    
            log.info("방 상태 브로드캐스트 완료: joinCode={}, playerCount={}", event.getJoinCode(), responses.size());
            
        } catch (Exception e) {
            log.error("방 상태 업데이트 이벤트 처리 실패: joinCode={}, reason={}", event.getJoinCode(), event.getReason(), e);
        }
    }
}
