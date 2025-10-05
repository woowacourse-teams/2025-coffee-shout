package coffeeshout.room.infra.messaging.handler;

import coffeeshout.global.lock.RedisLock;
import coffeeshout.global.ui.WebSocketResponse;
import coffeeshout.global.websocket.LoggingSimpMessagingTemplate;
import coffeeshout.room.domain.RoomState;
import coffeeshout.room.domain.event.RoomEventType;
import coffeeshout.room.domain.event.RouletteSpinEvent;
import coffeeshout.room.domain.player.Winner;
import coffeeshout.room.infra.persistence.PlayerEntity;
import coffeeshout.room.infra.persistence.PlayerJpaRepository;
import coffeeshout.room.infra.persistence.RoomEntity;
import coffeeshout.room.infra.persistence.RoomJpaRepository;
import coffeeshout.room.infra.persistence.RouletteResultEntity;
import coffeeshout.room.infra.persistence.RouletteResultJpaRepository;
import coffeeshout.room.ui.response.WinnerResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RouletteSpinEventHandler implements RoomEventHandler<RouletteSpinEvent> {

    private final LoggingSimpMessagingTemplate messagingTemplate;
    private final RoomJpaRepository roomJpaRepository;
    private final PlayerJpaRepository playerJpaRepository;
    private final RouletteResultJpaRepository rouletteResultJpaRepository;

    @Override
    public void handle(RouletteSpinEvent event) {
        try {
            log.info("룰렛 스핀 이벤트 수신: eventId={}, joinCode={}, hostName={}",
                    event.eventId(), event.joinCode(), event.hostName());

            final Winner winner = event.winner();

            broadcastWinner(event.joinCode(), winner);
            
            tryDbSave(event);

        } catch (Exception e) {
            log.error("룰렛 스핀 이벤트 처리 실패", e);
        }
    }

    private void broadcastWinner(String joinCode, Winner winner) {
        final WinnerResponse response = WinnerResponse.from(winner);
        messagingTemplate.convertAndSend("/topic/room/" + joinCode + "/winner",
                WebSocketResponse.success(response));
    }

    @RedisLock(
            key = "#event.eventId()",
            lockPrefix = "event:lock:",
            donePrefix = "event:done:",
            waitTime = 0,
            leaseTime = 5000
    )
    @Transactional
    public void tryDbSave(RouletteSpinEvent event) {
        final Winner winner = event.winner();
        
        // RoomEntity 조회 및 상태 업데이트
        final RoomEntity roomEntity = getRoomEntity(event.joinCode());
        roomEntity.updateRoomStatus(RoomState.DONE);
        roomEntity.finish();
        roomJpaRepository.save(roomEntity);  // ← 명시적 save 추가

        // PlayerEntity 조회
        final PlayerEntity playerEntity = getPlayerEntity(roomEntity, winner.name().value());

        // RouletteResultEntity 저장
        final RouletteResultEntity rouletteResult = new RouletteResultEntity(
                roomEntity,
                playerEntity,
                winner.probability()
        );
        rouletteResultJpaRepository.save(rouletteResult);

        log.info("RouletteResultEntity 저장 완료: joinCode={}, winner={}, probability={}",
                event.joinCode(), winner.name().value(), winner.probability());
        log.info("룰렛 스핀 이벤트 처리 완료 (DB 저장): eventId={}, joinCode={}, winner={}",
                event.eventId(), event.joinCode(), winner.name().value());
    }

    private RoomEntity getRoomEntity(String joinCode) {
        return roomJpaRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> new IllegalArgumentException("RoomEntity를 찾을 수 없습니다: " + joinCode));
    }

    private PlayerEntity getPlayerEntity(RoomEntity roomEntity, String playerName) {
        return playerJpaRepository.findByRoomSessionAndPlayerName(roomEntity, playerName)
                .orElseThrow(() -> new IllegalArgumentException("PlayerEntity를 찾을 수 없습니다: " + playerName));
    }

    @Override
    public RoomEventType getSupportedEventType() {
        return RoomEventType.ROULETTE_SPIN;
    }
}
