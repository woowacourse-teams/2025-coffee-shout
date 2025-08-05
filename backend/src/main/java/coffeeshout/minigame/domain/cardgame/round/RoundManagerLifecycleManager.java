package coffeeshout.minigame.domain.cardgame.round;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 애플리케이션 생명주기에 따른 RoundManager 정리를 담당하는 컴포넌트
 */
@Slf4j
@Component
public class RoundManagerLifecycleManager {
    
    private final RoundManagerRegistry roundManagerRegistry;
    
    public RoundManagerLifecycleManager(RoundManagerRegistry roundManagerRegistry) {
        this.roundManagerRegistry = roundManagerRegistry;
    }
    
    /**
     * 애플리케이션 종료 시 모든 RoundManager를 정리합니다.
     */
    @PreDestroy
    public void shutdown() {
        log.info("애플리케이션 종료 - RoundManager 정리 시작");
        roundManagerRegistry.removeAll();
        log.info("애플리케이션 종료 - RoundManager 정리 완료");
    }
}
