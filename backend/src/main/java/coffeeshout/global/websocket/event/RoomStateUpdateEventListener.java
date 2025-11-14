package coffeeshout.global.websocket.event;

import coffeeshout.room.application.RoomService;
import coffeeshout.room.domain.event.PlayerListUpdateEvent;
import coffeeshout.room.domain.event.RoomEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 방 상태 변경 이벤트 리스너
 * 플레이어 연결 해제, 강퇴 등 방 상태가 변경되면 모든 클라이언트에게 플레이어 목록을 업데이트합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoomStateUpdateEventListener {

    private final RoomService roomService;
    private final RoomEventPublisher roomEventPublisher;

    @Async
    @EventListener
    public void handleRoomStateUpdate(RoomStateUpdateEvent event) {
        try {
            if (!roomService.roomExists(event.joinCode())) {
                log.warn("존재하지 않는 방의 상태 업데이트 이벤트: joinCode={}", event.joinCode());
                return;
            }

            log.info("방 상태 업데이트 이벤트 수신: joinCode={}, reason={}", event.joinCode(), event.reason());

            // 플레이어 목록 업데이트 이벤트 발행
            roomEventPublisher.publish(new PlayerListUpdateEvent(event.joinCode()));

        } catch (Exception e) {
            log.error("방 상태 업데이트 이벤트 처리 실패: joinCode={}", event.joinCode(), e);
        }
    }
}
