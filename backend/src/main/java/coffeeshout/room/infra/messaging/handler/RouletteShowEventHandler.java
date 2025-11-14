package coffeeshout.room.infra.messaging.handler;

import coffeeshout.room.application.RoomEventHandler;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.event.RoomEventType;
import coffeeshout.room.domain.event.RouletteShowEvent;
import coffeeshout.room.domain.event.broadcast.RouletteShownEvent;
import coffeeshout.room.domain.service.RouletteCommandService;
import coffeeshout.room.infra.persistence.RoomPersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RouletteShowEventHandler implements RoomEventHandler<RouletteShowEvent> {

    private final RouletteCommandService rouletteCommandService;
    private final RoomPersistenceService roomPersistenceService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void handle(RouletteShowEvent event) {
        try {
            log.info("룰렛 전환 이벤트 수신: eventId={}, joinCode={}", event.eventId(), event.joinCode());

            final Room room = rouletteCommandService.showRoulette(event.joinCode());

            // Spring Domain Event 발행 - RoomMessagePublisher가 브로드캐스트 처리
            eventPublisher.publishEvent(
                    new RouletteShownEvent(
                            room.getJoinCode().getValue(), room.getRoomState()
                    )
            );

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
