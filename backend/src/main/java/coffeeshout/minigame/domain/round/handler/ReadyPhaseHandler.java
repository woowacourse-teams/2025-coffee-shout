package coffeeshout.minigame.domain.round.handler;

import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.round.RoundPhase;
import coffeeshout.minigame.domain.round.RoundPhaseHandler;
import coffeeshout.room.domain.Room;
import java.time.Duration;
import org.springframework.stereotype.Component;

@Component
public class ReadyPhaseHandler implements RoundPhaseHandler {
    @Override
    public void execute(CardGame game, Room room) {
    
    }

    @Override
    public boolean canSkipToNext(CardGame game) {
        return false;
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
