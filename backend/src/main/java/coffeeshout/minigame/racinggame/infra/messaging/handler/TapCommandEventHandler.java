package coffeeshout.minigame.racinggame.infra.messaging.handler;

import coffeeshout.minigame.racinggame.application.RacingGameCommandService;
import coffeeshout.minigame.racinggame.domain.event.RacingGameEventType;
import coffeeshout.minigame.racinggame.domain.event.TapCommandEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Redis 이벤트를 수신하여 Command Service로 라우팅하는 핸들러
 * 메시지 수신과 로깅만 담당하며, 비즈니스 로직은 CommandService에 위임합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TapCommandEventHandler implements RacingGameEventHandler<TapCommandEvent> {

    private final RacingGameCommandService racingGameCommandService;

    @Override
    public void handle(TapCommandEvent event) {
        try {
            log.debug("탭 이벤트 수신: eventId={}, joinCode={}, playerName={}, tapCount={}",
                    event.eventId(), event.joinCode(), event.playerName(), event.tapCount());

            racingGameCommandService.processTap(
                    event.joinCode(),
                    event.playerName(),
                    event.tapCount()
            );

        } catch (Exception e) {
            log.error("탭 이벤트 처리 실패: eventId={}, joinCode={}",
                    event.eventId(), event.joinCode(), e);
        }
    }

    @Override
    public RacingGameEventType getSupportedEventType() {
        return RacingGameEventType.TAP_COMMAND;
    }
}
