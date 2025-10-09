package coffeeshout.racinggame.application;

import coffeeshout.racinggame.domain.event.TapCommandEvent;
import coffeeshout.racinggame.infra.messaging.RacingGameEventPublisher;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RacingGameService {

    private final RacingGameEventPublisher racingGameEventPublisher;

    public void tap(String joinCode, String hostName, int tapCount) {
        final TapCommandEvent event = TapCommandEvent.create(joinCode, hostName, tapCount, Instant.now());
        racingGameEventPublisher.publishEvent(event);
        log.debug("탭 이벤트 발행: joinCode={}, playerName={}, tapCount={}, eventId={}",
                joinCode, hostName, tapCount, event.eventId());
    }
}
