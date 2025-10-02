package coffeeshout.minigame.infra.messaging.handler;

import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.minigame.domain.MiniGameScore;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.CardGameTaskType;
import coffeeshout.minigame.domain.cardgame.event.dto.CardGameStateChangedEvent;
import coffeeshout.minigame.infra.persistence.MiniGameEntity;
import coffeeshout.minigame.infra.persistence.MiniGameJpaRepository;
import coffeeshout.minigame.infra.persistence.MiniGameResultEntity;
import coffeeshout.minigame.infra.persistence.MiniGameResultJpaRepository;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.infra.persistance.PlayerEntity;
import coffeeshout.room.infra.persistance.PlayerJpaRepository;
import coffeeshout.room.infra.persistance.RoomEntity;
import coffeeshout.room.infra.persistance.RoomJpaRepository;
import java.time.Duration;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MiniGameResultSaveEventHandler {

    private final RoomJpaRepository roomJpaRepository;
    private final PlayerJpaRepository playerJpaRepository;
    private final MiniGameJpaRepository miniGameJpaRepository;
    private final MiniGameResultJpaRepository miniGameResultJpaRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @EventListener
    @Transactional
    public void handle(CardGameStateChangedEvent event) {
        if (event.currentTask() != CardGameTaskType.GAME_FINISH_STATE) {
            return;
        }

        tryDbSaveResult(event);
    }

    private void tryDbSaveResult(CardGameStateChangedEvent event) {
        final String joinCode = event.room().getJoinCode().getValue();
        final String lockKey = "minigame:result:lock:" + event.eventId();
        final String doneKey = "minigame:result:done:" + event.eventId();

        if (isAlreadyProcessed(doneKey, joinCode)) {
            return;
        }

        if (!acquireLock(lockKey, joinCode)) {
            return;
        }

        try {
            saveToDatabase(event, doneKey, joinCode);
        } finally {
            releaseLock(lockKey);
        }
    }

    private boolean isAlreadyProcessed(String doneKey, String joinCode) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(doneKey))) {
            log.debug("이미 처리된 미니게임 결과 (DB 저장 스킵): joinCode={}", joinCode);
            return true;
        }
        return false;
    }

    private boolean acquireLock(String lockKey, String joinCode) {
        final Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "locked", Duration.ofSeconds(5));

        if (!Boolean.TRUE.equals(acquired)) {
            log.debug("다른 인스턴스가 미니게임 결과 DB 저장 중: joinCode={}", joinCode);
            return false;
        }
        return true;
    }

    private void saveToDatabase(CardGameStateChangedEvent event, String doneKey, String joinCode) {
        final RoomEntity roomEntity = roomJpaRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> new IllegalArgumentException("방이 존재하지 않습니다: " + joinCode));

        final MiniGameEntity miniGameEntity = miniGameJpaRepository
                .findByRoomSessionAndMiniGameType(roomEntity, MiniGameType.CARD_GAME)
                .orElseThrow(() -> new IllegalArgumentException("카드게임 엔티티가 존재하지 않습니다: " + joinCode));

        final CardGame cardGame = event.cardGame();
        final MiniGameResult result = cardGame.getResult();
        final Map<Player, MiniGameScore> scores = cardGame.getScores();

        for (Player player : event.room().getPlayers()) {
            final PlayerEntity playerEntity = playerJpaRepository.findByRoomSessionAndPlayerName(roomEntity,
                            player.getName().value())
                    .orElseThrow(() -> new IllegalArgumentException("플레이어가 존재하지 않습니다: " + player.getName().value()));

            final Integer rank = result.getPlayerRank(player);
            final Integer score = scores.get(player).getValue();

            final MiniGameResultEntity resultEntity = new MiniGameResultEntity(
                    miniGameEntity,
                    playerEntity,
                    rank,
                    score
            );

            miniGameResultJpaRepository.save(resultEntity);
        }

        redisTemplate.opsForValue()
                .set(doneKey, "done", Duration.ofMinutes(10));

        log.info("미니게임 결과 저장 완료: joinCode={}", joinCode);
    }

    private void releaseLock(String lockKey) {
        redisTemplate.delete(lockKey);
    }
}
