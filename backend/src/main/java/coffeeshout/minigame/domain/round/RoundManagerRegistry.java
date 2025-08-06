package coffeeshout.minigame.domain.round;

import coffeeshout.room.domain.JoinCode;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

/**
 * 방별로 독립적인 RoundManager를 관리하는 레지스트리
 */
@Slf4j
@Component
public class RoundManagerRegistry {
    
    private final Map<JoinCode, RoomRoundManager> roomManagers = new ConcurrentHashMap<>();
    private final List<RoundPhaseHandler> handlerList;
    private final TaskScheduler scheduler;
    
    public RoundManagerRegistry(
            List<RoundPhaseHandler> handlerList,
            @Qualifier("miniGameTaskScheduler") TaskScheduler scheduler) {
        this.handlerList = handlerList;
        this.scheduler = scheduler;
    }
    
    /**
     * 방에 대한 RoundManager를 생성하거나 반환합니다.
     */
    public RoomRoundManager getOrCreate(JoinCode joinCode) {
        return roomManagers.computeIfAbsent(joinCode, this::createRoomRoundManager);
    }
    
    private RoomRoundManager createRoomRoundManager(JoinCode joinCode) {
        log.info("방 {}에 대한 새로운 RoundManager 생성", joinCode.value());
        return new RoomRoundManager(joinCode, handlerList, scheduler);
    }
    
    /**
     * 방의 RoundManager를 반환합니다.
     */
    public RoomRoundManager get(JoinCode joinCode) {
        return roomManagers.get(joinCode);
    }
    
    /**
     * 방이 종료될 때 해당 RoundManager를 정리합니다.
     */
    /**
     * 참조되는 인스턴스가 존재한다면, 해당 인스턴스의 스케줄링 작업이 제거되지 않을 수 있다.
     * 우선은 문제가 없으니 스케줄링 제거는 없애지 않는다.
     */

    public void remove(JoinCode joinCode) {
        roomManagers.remove(joinCode);
    }
    
    /**
     * 모든 방의 RoundManager를 정리합니다. (애플리케이션 종료 시)
     */
    @PreDestroy
    public void removeAll() {
        log.info("애플리케이션 종료 - 모든 RoundManager 정리 시작 (총 {} 개)", roomManagers.size());
        
        roomManagers.clear();
        
        log.info("애플리케이션 종료 - 모든 RoundManager 정리 완료");
    }
    
    /**
     * 현재 활성화된 방의 수를 반환합니다.
     */
    public int getActiveRoomCount() {
        return roomManagers.size();
    }
    
    /**
     * 특정 방이 등록되어 있는지 확인합니다.
     */
    public boolean exists(JoinCode joinCode) {
        return roomManagers.containsKey(joinCode);
    }
}
