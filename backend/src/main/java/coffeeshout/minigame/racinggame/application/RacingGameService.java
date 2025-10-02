package coffeeshout.minigame.racinggame.application;

import coffeeshout.minigame.racinggame.domain.event.StartRacingGameCommandEvent;
import coffeeshout.minigame.racinggame.domain.event.TapCommandEvent;
import coffeeshout.minigame.racinggame.infra.messaging.RacingGameEventPublisher;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RacingGameService {

    private final RacingGameEventPublisher racingGameEventPublisher;

    public void startGame(String joinCode, String hostName) {
        final StartRacingGameCommandEvent event = StartRacingGameCommandEvent.create(joinCode, hostName);
        racingGameEventPublisher.publishEvent(event);
        log.info("레이싱 게임 시작 이벤트 발행: joinCode={}, hostName={}, eventId={}",
                joinCode, hostName, event.eventId());
    }

    public void tap(String joinCode, String hostName, int tapCount) {
        final TapCommandEvent event = TapCommandEvent.create(joinCode, hostName, tapCount, Instant.now());
        racingGameEventPublisher.publishEvent(event);
        log.debug("탭 이벤트 발행: joinCode={}, playerName={}, tapCount={}, eventId={}",
                joinCode, hostName, tapCount, event.eventId());
    }
}
