package coffeeshout.room.infra.persistence;

import coffeeshout.global.lock.RedisLock;
import coffeeshout.room.domain.RoomState;
import coffeeshout.room.domain.event.RouletteShowEvent;
import coffeeshout.room.domain.event.RouletteSpinEvent;
import coffeeshout.room.domain.player.Winner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoulettePersistenceService {

    private final RoomJpaRepository roomJpaRepository;
    private final PlayerJpaRepository playerJpaRepository;
    private final RouletteResultJpaRepository rouletteResultJpaRepository;

    @RedisLock(
            key = "#event.eventId()",
            lockPrefix = "event:lock:",
            donePrefix = "event:done:",
            waitTime = 0,
            leaseTime = 5000
    )
    @Transactional
    public void saveRoomStatus(RouletteShowEvent event) {
        final RoomEntity roomEntity = getRoomEntity(event.joinCode());
        roomEntity.updateRoomStatus(RoomState.ROULETTE);

        log.info("룰렛 상태 DB 저장 완료: eventId={}, joinCode={}, status=ROULETTE",
                event.eventId(), event.joinCode());
    }

    @RedisLock(
            key = "#event.eventId()",
            lockPrefix = "event:lock:",
            donePrefix = "event:done:",
            waitTime = 0,
            leaseTime = 5000
    )
    @Transactional
    public void saveRouletteResult(RouletteSpinEvent event) {
        final Winner winner = event.winner();

        // RoomEntity 조회 및 상태 업데이트
        final RoomEntity roomEntity = getRoomEntity(event.joinCode());
        roomEntity.updateRoomStatus(RoomState.DONE);
        roomEntity.finish();

        // PlayerEntity 조회
        final PlayerEntity playerEntity = getPlayerEntity(roomEntity, winner.name().value());

        // RouletteResultEntity 저장
        final RouletteResultEntity rouletteResult = new RouletteResultEntity(
                roomEntity,
                playerEntity,
                winner.probability()
        );
        rouletteResultJpaRepository.save(rouletteResult);

        log.info("룰렛 결과 DB 저장 완료: eventId={}, joinCode={}, winner={}, probability={}",
                event.eventId(), event.joinCode(), winner.name().value(), winner.probability());
    }

    private RoomEntity getRoomEntity(String joinCode) {
        return roomJpaRepository.findFirstByJoinCodeOrderByCreatedAtDesc(joinCode)
                .orElseThrow(() -> new IllegalArgumentException("RoomEntity를 찾을 수 없습니다: " + joinCode));
    }

    private PlayerEntity getPlayerEntity(RoomEntity roomEntity, String playerName) {
        return playerJpaRepository.findByRoomSessionAndPlayerName(roomEntity, playerName)
                .orElseThrow(() -> new IllegalArgumentException("PlayerEntity를 찾을 수 없습니다: " + playerName));
    }
}
