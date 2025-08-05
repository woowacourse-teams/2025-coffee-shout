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
    private final boolean earlySkipEnabled;
    
    public PlayingPhaseHandler(
            @Value("${card-game.phases.playing.duration:PT10S}") Duration duration,
            @Value("${card-game.phases.playing.early-skip:true}") boolean earlySkipEnabled) {
        this.duration = duration;
        this.earlySkipEnabled = earlySkipEnabled;
    }
    
    @Override
    public void execute(CardGame game, Room room) {
        log.info("라운드 {} 플레이 시작", game.getRoundState().getRoundNumber());
        game.startPlay();
    }
    
    @Override
    public boolean canSkipToNext(CardGame game) {
        // 설정에서 조기 종료가 활성화되어 있고, 모든 플레이어가 카드를 선택했다면 조기 종료
        return earlySkipEnabled && game.isFinishedThisRound();
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
