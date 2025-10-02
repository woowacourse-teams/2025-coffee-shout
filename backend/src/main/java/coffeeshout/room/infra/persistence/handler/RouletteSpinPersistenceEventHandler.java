package coffeeshout.room.infra.persistence.handler;

import coffeeshout.room.domain.RoomState;
import coffeeshout.room.domain.event.RouletteSpinEvent;
import coffeeshout.room.domain.player.Winner;
import coffeeshout.room.infra.persistence.PlayerEntity;
import coffeeshout.room.infra.persistence.PlayerJpaRepository;
import coffeeshout.room.infra.persistence.RoomEntity;
import coffeeshout.room.infra.persistence.RoomJpaRepository;
import coffeeshout.room.infra.persistence.RouletteResultEntity;
import coffeeshout.room.infra.persistence.RouletteResultJpaRepository;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RouletteSpinPersistenceEventHandler {

    private final RoomJpaRepository roomJpaRepository;
    private final PlayerJpaRepository playerJpaRepository;
    private final RouletteResultJpaRepository rouletteResultJpaRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @EventListener
    @Transactional
    void handle(RouletteSpinEvent event) {
        final Winner winner = event.winner();

        final String lockKey = "event:lock:" + event.eventId();
        final String doneKey = "event:done:" + event.eventId();

        if (isAlreadyProcessed(doneKey, event.eventId())) {
            return;
        }

        if (!acquireLock(lockKey, event.eventId())) {
            return;
        }

        try {
            saveToDatabase(event, winner, doneKey);
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

    private void saveToDatabase(RouletteSpinEvent event, Winner winner, String doneKey) {
        saveRouletteResult(event.joinCode(), winner);

        redisTemplate.opsForValue()
                .set(doneKey, "done", Duration.ofMinutes(10));

        log.info("룰렛 스핀 이벤트 처리 완료 (DB 저장): eventId={}, joinCode={}, winner={}",
                event.eventId(), event.joinCode(), winner.name().value());
    }

    private void releaseLock(String lockKey) {
        redisTemplate.delete(lockKey);
    }

    private void saveRouletteResult(String joinCode, Winner winner) {
        final RoomEntity roomEntity = getRoomEntity(joinCode);
        roomEntity.updateRoomStatus(RoomState.DONE);
        roomEntity.finish();

        final PlayerEntity playerEntity = getPlayerEntity(roomEntity, winner.name().value());

        final RouletteResultEntity rouletteResult = new RouletteResultEntity(
                roomEntity,
                playerEntity,
                winner.probability()
        );
        rouletteResultJpaRepository.save(rouletteResult);

        log.info("RouletteResultEntity 저장 완료: joinCode={}, winner={}, probability={}",
                joinCode, winner.name().value(), winner.probability());
    }

    private RoomEntity getRoomEntity(String joinCode) {
        return roomJpaRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> new IllegalArgumentException("RoomEntity를 찾을 수 없습니다: " + joinCode));
    }

    private PlayerEntity getPlayerEntity(RoomEntity roomEntity, String playerName) {
        return playerJpaRepository.findByRoomSessionAndPlayerName(roomEntity, playerName)
                .orElseThrow(() -> new IllegalArgumentException("PlayerEntity를 찾을 수 없습니다: " + playerName));
    }
}
