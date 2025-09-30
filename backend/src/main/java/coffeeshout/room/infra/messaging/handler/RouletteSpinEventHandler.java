package coffeeshout.room.infra.messaging.handler;

import coffeeshout.global.ui.WebSocketResponse;
import coffeeshout.global.websocket.LoggingSimpMessagingTemplate;
import coffeeshout.room.domain.event.RoomEventType;
import coffeeshout.room.domain.event.RouletteSpinEvent;
import coffeeshout.room.domain.player.Winner;
import coffeeshout.room.ui.response.WinnerResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RouletteSpinEventHandler implements RoomEventHandler<RouletteSpinEvent> {

    private final LoggingSimpMessagingTemplate messagingTemplate;

    @Override
    public void handle(RouletteSpinEvent event) {
        try {
            log.info("룰렛 스핀 이벤트 수신: eventId={}, joinCode={}, hostName={}",
                    event.getEventId(), event.joinCode(), event.hostName());

            final Winner winner = event.winner();
            final WinnerResponse response = WinnerResponse.from(winner);

            messagingTemplate.convertAndSend("/topic/room/" + event.joinCode() + "/winner",
                    WebSocketResponse.success(response));

            log.info("룰렛 스핀 이벤트 처리 완료: eventId={}, joinCode={}, winner={}",
                    event.getEventId(), event.joinCode(), winner.name().value());

        } catch (Exception e) {
            log.error("룰렛 스핀 이벤트 처리 실패", e);
        }
    }

    @Override
    public RoomEventType getSupportedEventType() {
        return RoomEventType.ROULETTE_SPIN;
    }
}
