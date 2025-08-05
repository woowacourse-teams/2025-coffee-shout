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
 * 점수 계산 및 표시 단계를 처리하는 핸들러
 */
@Slf4j
@Component
public class ScoringPhaseHandler implements RoundPhaseHandler {
    
    private final Duration duration;
    
    public ScoringPhaseHandler(@Value("${card-game.phases.scoring.duration:PT1.5S}") Duration duration) {
        this.duration = duration;
    }
    
    @Override
    public void execute(CardGame game, Room room) {
        log.info("라운드 {} 점수 계산 시작", game.getRoundState().getRoundNumber());
        
        // 선택하지 않은 플레이어들에게 랜덤 카드 할당
        game.assignRandomCardsToUnselectedPlayers();
        
        // 점수판 상태로 변경
        game.changeScoreBoardState();
    }
    
    @Override
    public boolean canSkipToNext(CardGame game) {
        // 점수 표시는 항상 지정된 시간만큼 보여줌
        return false;
    }
    
    @Override
    public Duration getDuration() {
        return duration;
    }
    
    @Override
    public RoundPhase getPhase() {
        return RoundPhase.SCORING;
    }
}
