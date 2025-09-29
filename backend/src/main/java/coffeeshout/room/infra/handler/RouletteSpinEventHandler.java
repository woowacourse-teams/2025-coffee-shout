package coffeeshout.room.infra.handler;

import coffeeshout.global.ui.WebSocketResponse;
import coffeeshout.global.websocket.LoggingSimpMessagingTemplate;
import coffeeshout.room.domain.RoomState;
import coffeeshout.room.domain.event.RoomEventType;
import coffeeshout.room.domain.event.RouletteSpinEvent;
import coffeeshout.room.domain.player.Winner;
import coffeeshout.room.infra.persistance.PlayerEntity;
import coffeeshout.room.infra.persistance.PlayerJpaRepository;
import coffeeshout.room.infra.persistance.RoomEntity;
import coffeeshout.room.infra.persistance.RoomJpaRepository;
import coffeeshout.room.infra.persistance.RouletteResultEntity;
import coffeeshout.room.infra.persistance.RouletteResultJpaRepository;
import coffeeshout.room.ui.response.WinnerResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
                    event.getEventId(), event.joinCode(), event.hostName());

            final Winner winner = event.winner();
            final WinnerResponse response = WinnerResponse.from(winner);

            saveRouletteResult(event.joinCode(), winner);

            messagingTemplate.convertAndSend("/topic/room/" + event.joinCode() + "/winner",
                    WebSocketResponse.success(response));

            log.info("룰렛 스핀 이벤트 처리 완료: eventId={}, joinCode={}, winner={}",
                    event.getEventId(), event.joinCode(), winner.name().value());

        } catch (Exception e) {
            log.error("룰렛 스핀 이벤트 처리 실패", e);
        }
    }

    @Override
    public RoomEventType getSupportedEventType() {
        return RoomEventType.ROULETTE_SPIN;
    }

    private void saveRouletteResult(String joinCode, Winner winner) {
        final RoomEntity roomEntity = getRoomEntity(joinCode);
        roomEntity.updateRoomStatus(RoomState.DONE);

        final PlayerEntity playerEntity = getPlayerEntity(roomEntity, winner.name().value());

        RouletteResultEntity rouletteResult = new RouletteResultEntity(
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
