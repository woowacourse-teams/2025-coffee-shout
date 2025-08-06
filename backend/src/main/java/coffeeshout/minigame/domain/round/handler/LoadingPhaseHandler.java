package coffeeshout.minigame.domain.round.handler;

import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.round.RoundPhase;
import coffeeshout.minigame.domain.round.RoundPhaseHandler;
import coffeeshout.room.domain.Room;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 라운드 로딩 단계를 처리하는 핸들러
 */
@Slf4j
@Component
public class LoadingPhaseHandler implements RoundPhaseHandler {
    
    private final Duration duration;
    
    public LoadingPhaseHandler(@Value("${card-game.phases.loading.duration:PT3S}") Duration duration) {
        this.duration = duration;
    }
    
    @Override
    public void execute(CardGame game, Room room) {
        log.debug("라운드 {} 로딩 시작", game.getRoundState().roundNumber());
        game.startRound();
    }
    
    @Override
    public Duration getDuration() {
        return duration;
    }
    
    @Override
    public RoundPhase getPhase() {
        return RoundPhase.LOADING;
    }
}
