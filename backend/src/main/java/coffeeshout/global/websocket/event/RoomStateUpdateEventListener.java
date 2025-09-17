package coffeeshout.global.websocket.event;

import coffeeshout.room.application.RoomService;
import coffeeshout.room.infra.BroadcastEventPublisher;
import coffeeshout.room.ui.event.PlayerUpdateBroadcastEvent;
import coffeeshout.room.ui.event.ProbabilityUpdateBroadcastEvent;
import coffeeshout.room.ui.response.PlayerResponse;
import coffeeshout.room.ui.response.ProbabilityResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomStateUpdateEventListener {

    private final RoomService roomService;
    private final BroadcastEventPublisher broadcastEventPublisher;

    @EventListener
    public void handleRoomStateUpdate(RoomStateUpdateEvent event) {
        try {
            log.info("방 상태 업데이트 이벤트 처리: joinCode={}, reason={}", event.joinCode(), event.reason());

            broadcastRoomState(event.joinCode());
        } catch (Exception e) {
            log.error("방 상태 업데이트 이벤트 처리 실패: joinCode={}, reason={}", event.joinCode(), event.reason(), e);
        }
    }

    private void broadcastRoomState(String joinCode) {
        if (roomService.roomExists(joinCode)) {
            sendPlayerStatus(joinCode);
            sendProbabilitiesStatus(joinCode);
            log.info("방 상태 브로드캐스트 완료: joinCode={}", joinCode);
        }
    }

    private void sendPlayerStatus(String joinCode) {
        final List<PlayerResponse> responses = roomService.getAllPlayers(joinCode)
                .stream()
                .map(PlayerResponse::from)
                .toList();

        final PlayerUpdateBroadcastEvent event = PlayerUpdateBroadcastEvent.create(joinCode, responses);
        broadcastEventPublisher.publishPlayerUpdateEvent(event);
    }

    private void sendProbabilitiesStatus(String joinCode) {
        final List<ProbabilityResponse> responses = roomService.getProbabilities(joinCode).entrySet()
                .stream()
                .map(ProbabilityResponse::from)
                .toList();

        final ProbabilityUpdateBroadcastEvent event = ProbabilityUpdateBroadcastEvent.create(joinCode, responses);
        broadcastEventPublisher.publishProbabilityUpdateEvent(event);
    }
}
