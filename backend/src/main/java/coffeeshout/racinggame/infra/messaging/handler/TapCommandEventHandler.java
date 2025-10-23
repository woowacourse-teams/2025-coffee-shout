package coffeeshout.racinggame.infra.messaging.handler;

import coffeeshout.racinggame.application.RacingGameService;
import coffeeshout.racinggame.domain.event.RacingGameEventType;
import coffeeshout.racinggame.domain.event.TapCommandEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TapCommandEventHandler implements RacingGameEventHandler<TapCommandEvent> {

    private final RacingGameService racingGameService;

    @Override
    public void handle(TapCommandEvent event) {
        log.debug("탭 이벤트 수신: eventId={}, joinCode={}, playerName={}, tapCount={}",
                event.eventId(), event.joinCode(), event.playerName(), event.tapCount());

        racingGameService.tap(
                event.joinCode(),
                event.playerName(),
                event.tapCount()
        );
    }

    @Override
    public RacingGameEventType getSupportedEventType() {
        return RacingGameEventType.TAP_COMMAND;
    }
}
