package coffeeshout.racinggame.infra.messaging.handler;

import coffeeshout.global.redis.EventHandler;
import coffeeshout.racinggame.application.RacingGameService;
import coffeeshout.racinggame.domain.event.TapCommandEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TapCommandEventHandler implements EventHandler<TapCommandEvent> {

    private final RacingGameService racingGameService;

    @Override
    public void handle(TapCommandEvent event) {
        racingGameService.tap(event.joinCode(), event.playerName(), event.tapCount());
    }

    @Override
    public Class<TapCommandEvent> eventType() {
        return TapCommandEvent.class;
    }
}
