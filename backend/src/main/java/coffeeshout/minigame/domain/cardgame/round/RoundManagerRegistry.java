package coffeeshout.minigame.domain.cardgame.round;

import coffeeshout.room.domain.JoinCode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 방별로 독립적인 RoundManager를 관리하는 레지스트리
 */
@Slf4j
@Component
public class RoundManagerRegistry {
    
    private final Map<JoinCode, RoomRoundManager> roomManagers = new ConcurrentHashMap<>();
    private final RoundManagerFactory roundManagerFactory;
    
    public RoundManagerRegistry(RoundManagerFactory roundManagerFactory) {
        this.roundManagerFactory = roundManagerFactory;
    }
    
    /**
     * 방에 대한 RoundManager를 생성하거나 반환합니다.
     */
    public RoomRoundManager getOrCreate(JoinCode joinCode) {
        return roomManagers.computeIfAbsent(joinCode, key -> {
            log.info("방 {}에 대한 새로운 RoundManager 생성", key.value());
            return roundManagerFactory.create(key);
        });
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
    public void remove(JoinCode joinCode) {
        RoomRoundManager manager = roomManagers.remove(joinCode);
        if (manager != null) {
            log.info("방 {} RoundManager 정리 시작", joinCode.value());
            manager.cleanup();
            log.info("방 {} RoundManager 정리 완료", joinCode.value());
        }
    }
    
    /**
     * 모든 방의 RoundManager를 정리합니다. (애플리케이션 종료 시)
     */
    public void removeAll() {
        log.info("모든 RoundManager 정리 시작 (총 {} 개)", roomManagers.size());
        
        roomManagers.values().forEach(RoomRoundManager::cleanup);
        roomManagers.clear();
        
        log.info("모든 RoundManager 정리 완료");
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
