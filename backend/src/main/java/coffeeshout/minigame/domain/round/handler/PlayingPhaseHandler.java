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
 * 카드 선택 단계를 처리하는 핸들러
 */
@Slf4j
@Component
public class PlayingPhaseHandler implements RoundPhaseHandler {
    
    private final Duration duration;

    public PlayingPhaseHandler(
            @Value("${card-game.phases.playing.duration:PT10S}") Duration duration,
            @Value("${card-game.phases.playing.early-skip:true}") boolean earlySkipEnabled) {
        this.duration = duration;
    }
    
    @Override
    public void execute(CardGame game, Room room) {
        log.debug("라운드 {} 플레이 시작", game.getRoundState().roundNumber());
        game.startPlay();
    }

    @Override
    public Duration getDuration() {
        return duration;
    }
    
    @Override
    public RoundPhase getPhase() {
        return RoundPhase.PLAYING;
    }
}
