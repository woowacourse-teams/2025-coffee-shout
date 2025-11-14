package coffeeshout.room.infra.messaging.handler;

import coffeeshout.room.application.RoomEventHandler;
import coffeeshout.room.domain.event.RoomEventType;
import coffeeshout.room.domain.event.RouletteSpinEvent;
import coffeeshout.room.domain.event.broadcast.RouletteWinnerSelectedEvent;
import coffeeshout.room.domain.player.Winner;
import coffeeshout.room.infra.persistence.RoomPersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RouletteSpinEventHandler implements RoomEventHandler<RouletteSpinEvent> {

    private final ApplicationEventPublisher eventPublisher;
    private final RoomPersistenceService roomPersistenceService;

    @Override
    public void handle(RouletteSpinEvent event) {
        try {
            log.info("룰렛 스핀 이벤트 수신: eventId={}, joinCode={}, hostName={}",
                    event.eventId(), event.joinCode(), event.hostName());

            final Winner winner = event.winner();

            // Spring Domain Event 발행 - RoomMessagePublisher가 브로드캐스트 처리
            eventPublisher.publishEvent(new RouletteWinnerSelectedEvent(event.joinCode(), winner));

            // DB 저장 (락으로 보호 - 중복 저장 방지)
            roomPersistenceService.saveRouletteResult(event);

            log.info("룰렛 스핀 이벤트 처리 완료: eventId={}, joinCode={}, winner={}",
                    event.eventId(), event.joinCode(), winner.name().value());

        } catch (Exception e) {
            log.error("룰렛 스핀 이벤트 처리 실패", e);
        }
    }

    @Override
    public RoomEventType getSupportedEventType() {
        return RoomEventType.ROULETTE_SPIN;
    }
}
