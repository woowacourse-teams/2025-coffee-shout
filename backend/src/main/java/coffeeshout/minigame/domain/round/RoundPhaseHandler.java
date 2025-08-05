package coffeeshout.minigame.domain.round;

import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.room.domain.Room;
import java.time.Duration;

/**
 * 라운드 단계별 처리 로직을 정의하는 인터페이스
 */
public interface RoundPhaseHandler {
    
    /**
     * 해당 단계에서 처리할 로직을 실행합니다.
     */
    void execute(CardGame game, Room room);
    
    /**
     * 이 단계에서 다음 단계로 조기 전환이 가능한지 확인합니다.
     * 예: 모든 플레이어가 카드 선택을 완료한 경우
     */
    boolean canSkipToNext(CardGame game);
    
    /**
     * 이 단계의 지속 시간을 반환합니다.
     * 설정에 따라 동적으로 변경될 수 있습니다.
     */
    Duration getDuration();
    
    /**
     * 이 핸들러가 처리하는 단계를 반환합니다.
     */
    RoundPhase getPhase();
}
