package coffeeshout.room.application.handler;

import coffeeshout.global.ui.WebSocketResponse;
import coffeeshout.global.websocket.LoggingSimpMessagingTemplate;
import coffeeshout.room.application.RoomEventHandler;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.event.RoomEventType;
import coffeeshout.room.domain.event.RouletteShowEvent;
import coffeeshout.room.domain.service.RoomQueryService;
import coffeeshout.room.infra.persistence.RoomPersistenceService;
import coffeeshout.room.ui.response.RoomStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RouletteShowEventHandler implements RoomEventHandler<RouletteShowEvent> {

    private final RoomQueryService roomQueryService;
    private final RoomPersistenceService roomPersistenceService;
    private final LoggingSimpMessagingTemplate messagingTemplate;

    @Override
    public void handle(RouletteShowEvent event) {
        try {
            log.info("룰렛 전환 이벤트 수신: eventId={}, joinCode={}", event.eventId(), event.joinCode());

            final Room room = roomQueryService.getByJoinCode(new JoinCode(event.joinCode()));
            room.showRoulette();

            final RoomStatusResponse response = RoomStatusResponse.of(room.getJoinCode(), room.getRoomState());

            messagingTemplate.convertAndSend("/topic/room/" + event.joinCode() + "/roulette",
                    WebSocketResponse.success(response));

            // DB 저장 (락으로 보호 - 중복 저장 방지)
            roomPersistenceService.saveRoomStatus(event);

            log.info("룰렛 전환 이벤트 처리 완료: eventId={}, joinCode={}",
                    event.eventId(), event.joinCode());

        } catch (Exception e) {
            log.error("룰렛 전환 이벤트 처리 실패: eventId={}, joinCode={}",
                    event.eventId(), event.joinCode(), e);
        }
    }

    @Override
    public RoomEventType getSupportedEventType() {
        return RoomEventType.ROULETTE_SHOW;
    }
}
