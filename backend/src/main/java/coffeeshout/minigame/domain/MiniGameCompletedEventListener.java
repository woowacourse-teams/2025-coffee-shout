package coffeeshout.minigame.domain;

import coffeeshout.global.config.InstanceConfig;
import coffeeshout.global.redis.RedisMessagePublisher;
import coffeeshout.global.redis.event.minigame.MiniGameCompletedEvent;
import coffeeshout.minigame.domain.dto.MiniGameCompletedLocalEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MiniGameCompletedEventListener {

    private final RedisMessagePublisher messagePublisher;
    private final InstanceConfig instanceConfig;

    @EventListener
    public void handleMiniGameCompleted(MiniGameCompletedLocalEvent event) {
        try {
            // Redis로 미니게임 완료 이벤트 발행
            messagePublisher.publishMiniGameCompleted(new MiniGameCompletedEvent(
                event.joinCode().getValue(),
                event.miniGameType(),
                event.result(),
                instanceConfig.getInstanceId()
            ));
            
            log.debug("미니게임 완료 이벤트 발행: joinCode={}, miniGameType={}", 
                     event.joinCode().getValue(), event.miniGameType());
        } catch (Exception e) {
            log.error("미니게임 완료 이벤트 발행 실패: joinCode={}, error={}", 
                     event.joinCode().getValue(), e.getMessage(), e);
        }
    }
}
