package coffeeshout.minigame.domain.cardgame.round;

import coffeeshout.room.domain.JoinCode;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

/**
 * 방별 RoundManager 생성을 담당하는 팩토리
 */
@Component
public class RoundManagerFactory {
    
    private final List<RoundPhaseHandler> handlerList;
    private final TaskScheduler scheduler;
    
    public RoundManagerFactory(
            List<RoundPhaseHandler> handlerList,
            @Qualifier("miniGameTaskScheduler") TaskScheduler scheduler) {
        this.handlerList = handlerList;
        this.scheduler = scheduler;
    }
    
    /**
     * 특정 방에 대한 RoomRoundManager를 생성합니다.
     */
    public RoomRoundManager create(JoinCode joinCode) {
        return new RoomRoundManager(joinCode, handlerList, scheduler);
    }
}
