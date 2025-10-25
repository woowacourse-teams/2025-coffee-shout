package coffeeshout.racinggame.infra.messaging.handler;

import coffeeshout.global.redis.BaseEvent;
import coffeeshout.global.redis.EventHandler;
import coffeeshout.racinggame.application.RacingGameService;
import coffeeshout.racinggame.domain.event.TapCommandEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TapCommandEventHandler implements EventHandler {

    private final RacingGameService racingGameService;

    @Override
    public void handle(BaseEvent event) {
        final TapCommandEvent tapCommandEvent = (TapCommandEvent) event;
        racingGameService.tap(tapCommandEvent.joinCode(), tapCommandEvent.playerName(), tapCommandEvent.tapCount());
    }

    @Override
    public Class<?> eventType() {
        return TapCommandEvent.class;
    }
}
