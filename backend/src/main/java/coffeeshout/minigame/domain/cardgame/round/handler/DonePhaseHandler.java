package coffeeshout.minigame.domain.cardgame.round.handler;

import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.round.RoundPhase;
import coffeeshout.minigame.domain.cardgame.round.RoundPhaseHandler;
import coffeeshout.room.domain.Room;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 게임 완료 단계를 처리하는 핸들러
 */
@Slf4j
@Component
public class DonePhaseHandler implements RoundPhaseHandler {
    
    @Override
    public void execute(CardGame game, Room room) {
        log.info("카드게임 완료");
        
        // 게임 완료 상태로 변경
        game.changeDoneState();
        
        // 최종 결과를 룸에 적용
        MiniGameResult result = game.getResult();
        room.applyMiniGameResult(result);
    }
    
    @Override
    public boolean canSkipToNext(CardGame game) {
        // 게임이 완료되면 더 이상 진행할 단계가 없음
        return false;
    }
    
    @Override
    public Duration getDuration() {
        // 게임 완료 상태는 지속 시간이 없음
        return Duration.ZERO;
    }
    
    @Override
    public RoundPhase getPhase() {
        return RoundPhase.DONE;
    }
}
