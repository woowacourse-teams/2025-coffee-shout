package coffeeshout.room.infra.persistence.handler;

import coffeeshout.global.lock.RedisLock;
import coffeeshout.room.domain.RoomState;
import coffeeshout.room.domain.event.RouletteSpinEvent;
import coffeeshout.room.domain.player.Winner;
import coffeeshout.room.infra.persistence.PlayerEntity;
import coffeeshout.room.infra.persistence.PlayerJpaRepository;
import coffeeshout.room.infra.persistence.RoomEntity;
import coffeeshout.room.infra.persistence.RoomJpaRepository;
import coffeeshout.room.infra.persistence.RouletteResultEntity;
import coffeeshout.room.infra.persistence.RouletteResultJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RouletteSpinPersistenceEventHandler {

    private final RoomJpaRepository roomJpaRepository;
    private final PlayerJpaRepository playerJpaRepository;
    private final RouletteResultJpaRepository rouletteResultJpaRepository;

    @EventListener
    @Transactional
    @RedisLock(
            key = "#event.eventId()",
            lockPrefix = "event:lock:",
            donePrefix = "event:done:",
            waitTime = 0,
            leaseTime = 5000
    )
    void handle(RouletteSpinEvent event) {
        final Winner winner = event.winner();
        saveRouletteResult(event.joinCode(), winner);
        log.info("룰렛 스핀 이벤트 처리 완료 (DB 저장): eventId={}, joinCode={}, winner={}",
                event.eventId(), event.joinCode(), winner.name().value());
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
