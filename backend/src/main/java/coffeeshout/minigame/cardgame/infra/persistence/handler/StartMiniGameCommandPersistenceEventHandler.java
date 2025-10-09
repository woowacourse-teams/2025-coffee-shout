package coffeeshout.minigame.cardgame.infra.persistence.handler;

import coffeeshout.global.lock.RedisLock;
import coffeeshout.minigame.cardgame.application.CardGameService;
import coffeeshout.minigame.event.StartMiniGameCommandEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartMiniGameCommandPersistenceEventHandler {

    private final CardGameService cardGameService;

    @EventListener
    @Transactional
    @RedisLock(
            key = "#event.eventId()",
            lockPrefix = "event:lock:",
            donePrefix = "event:done:",
            waitTime = 0,
            leaseTime = 5000
    )
    public void handle(StartMiniGameCommandEvent event) {
        cardGameService.saveGameEntities(event.joinCode());
        log.info("미니게임 시작 이벤트 처리 완료 (DB 저장): eventId={}, joinCode={}",
                event.eventId(), event.joinCode());
    }
}
