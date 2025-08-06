package coffeeshout.minigame.domain.round.handler;

import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.round.RoundPhase;
import coffeeshout.minigame.domain.round.RoundPhaseHandler;
import coffeeshout.room.domain.Room;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ReadyPhaseHandler implements RoundPhaseHandler {
    @Override
    public void execute(CardGame game, Room room) {
        log.debug("라운드 {} 로딩 시작", game.getRoundState().getRoundNumber());
    }

    @Override
    public Duration getDuration() {
        return Duration.ZERO;
    }

    @Override
    public RoundPhase getPhase() {
        return RoundPhase.READY;
    }
}
