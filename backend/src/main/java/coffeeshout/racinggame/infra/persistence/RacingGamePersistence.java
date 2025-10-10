package coffeeshout.racinggame.infra.persistence;

import coffeeshout.global.lock.RedisLock;
import coffeeshout.racinggame.application.RacingGameCommandService;
import coffeeshout.racinggame.domain.event.RaceStateChangedEvent;
import coffeeshout.room.domain.JoinCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RacingGamePersistence {

    private final RacingGameCommandService racingGameCommandService;

    @EventListener
    @Transactional
    @RedisLock(
            key = "#event.eventId()",
            lockPrefix = "minigame:result:lock:",
            donePrefix = "minigame:result:done:",
            waitTime = 0,
            leaseTime = 5000
    )
    public void saveResults(String joinCode) {
        final JoinCode roomJoinCode = new JoinCode(joinCode);
    }

    @EventListener
    @Transactional
    @RedisLock(
            key = "#event.eventId()",
            lockPrefix = "minigame:result:lock:",
            donePrefix = "minigame:result:done:",
            waitTime = 0,
            leaseTime = 5000
    )
    public void saveGame(RaceStateChangedEvent raceStateChangedEvent) {
//        if (raceStateChangedEvent.state().equals(RacingGameState.DESCRIPTION.name()))
//        final JoinCode roomJoinCode = new JoinCode(joinCode);
//        racingGameCommandService.saveGameEntities(joinCode);
    }

}
