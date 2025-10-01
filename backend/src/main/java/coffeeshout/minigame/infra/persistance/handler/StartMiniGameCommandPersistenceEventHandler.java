package coffeeshout.minigame.infra.persistance.handler;

import coffeeshout.minigame.application.CardGameService;
import coffeeshout.minigame.domain.event.StartMiniGameCommandEvent;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartMiniGameCommandPersistenceEventHandler {

    private final CardGameService cardGameService;
    private final RedisTemplate<String, String> redisTemplate;

    @EventListener
    public void handle(StartMiniGameCommandEvent event) {
        final String lockKey = "event:lock:" + event.eventId();
        final String doneKey = "event:done:" + event.eventId();

        if (isAlreadyProcessed(doneKey, event.eventId())) {
            return;
        }

        if (!acquireLock(lockKey, event.eventId())) {
            return;
        }

        try {
            saveToDatabase(event, doneKey);
        } finally {
            releaseLock(lockKey);
        }
    }

    private boolean isAlreadyProcessed(String doneKey, String eventId) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(doneKey))) {
            log.debug("이미 처리된 이벤트 (DB 저장 스킵): eventId={}", eventId);
            return true;
        }
        return false;
    }

    private boolean acquireLock(String lockKey, String eventId) {
        final Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "locked", Duration.ofSeconds(5));

        if (!Boolean.TRUE.equals(acquired)) {
            log.debug("다른 인스턴스가 DB 저장 중: eventId={}", eventId);
            return false;
        }
        return true;
    }

    private void saveToDatabase(StartMiniGameCommandEvent event, String doneKey) {
        if (isAlreadyProcessed(doneKey, event.eventId())) {
            return;
        }

        cardGameService.saveGameEntities(event.joinCode());

        redisTemplate.opsForValue()
                .set(doneKey, "done", Duration.ofMinutes(10));

        log.info("미니게임 시작 이벤트 처리 완료 (DB 저장): eventId={}, joinCode={}",
                event.eventId(), event.joinCode());
    }

    private void releaseLock(String lockKey) {
        redisTemplate.delete(lockKey);
    }

}
