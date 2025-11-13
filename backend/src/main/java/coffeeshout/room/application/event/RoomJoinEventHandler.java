package coffeeshout.room.application.event;

import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.event.RoomJoinEvent;
import coffeeshout.room.domain.menu.Menu;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.service.MenuCommandService;
import coffeeshout.room.domain.service.RoomCommandService;
import coffeeshout.room.infra.messaging.RoomEventWaitManager;
import coffeeshout.room.ui.request.SelectedMenuRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 방 입장 이벤트 핸들러
 * - Stream Consumer에서 발행한 RoomJoinEvent 처리
 * - 실제 도메인 로직 실행 (joinGuest)
 * - CompletableFuture 완료 처리 (REST API 응답)
 *
 * 흐름:
 * 1. REST API → RoomService.enterRoomAsync()
 * 2. CompletableFuture 등록
 * 3. Redis Stream 발행
 * 4. RoomEnterStreamConsumer 수신
 * 5. ApplicationEvent 발행
 * 6. 이 핸들러가 처리 → Future 완료
 * 7. REST API 응답 반환
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoomJoinEventHandler {

    private final RoomCommandService roomCommandService;
    private final MenuCommandService menuCommandService;
    private final RoomEventWaitManager roomEventWaitManager;

    /**
     * 방 입장 이벤트 처리
     *
     * @Order(1) - 가장 먼저 실행되어야 함
     * - 다른 핸들러(Local, Redis Bridge 등)보다 먼저 실행
     * - 비즈니스 로직 완료 후 Future 완료하여 REST API 응답
     *
     * 참고:
     * - @Async를 사용하지 않음 (Stream Consumer 스레드에서 직접 실행)
     * - Future 완료가 빨라야 REST API 응답이 빠름
     */
    @EventListener
    @Order(1)  // 우선순위 최상위
    public void handleRoomJoin(RoomJoinEvent event) {
        log.info("[방 입장 핸들러] 이벤트 처리 시작: eventId={}, joinCode={}, guestName={}",
            event.eventId(), event.joinCode(), event.guestName());

        try {
            // 1. 도메인 로직 실행
            SelectedMenuRequest selectedMenuRequest = event.selectedMenuRequest();

            Menu menu = menuCommandService.convertMenu(
                selectedMenuRequest.id(),
                selectedMenuRequest.customName()
            );

            Room room = roomCommandService.joinGuest(
                new JoinCode(event.joinCode()),
                new PlayerName(event.guestName()),
                menu,
                selectedMenuRequest.temperature()
            );

            log.info("[방 입장 핸들러] 방 입장 성공: joinCode={}, guestName={}, 현재 인원={}, eventId={}",
                    event.joinCode(), event.guestName(), room.getPlayers().size(),
                    event.eventId());

            // 2. CompletableFuture 완료 (REST API 응답)
            roomEventWaitManager.notifySuccess(event.eventId(), room);

            log.info("[방 입장 핸들러] Future 완료: eventId={}", event.eventId());

        } catch (Exception e) {
            log.error("[방 입장 핸들러] 방 입장 처리 실패: eventId={}, joinCode={}, guestName={}, error={}",
                    event.eventId(), event.joinCode(), event.guestName(), e.getMessage(), e);

            // 3. CompletableFuture 실패 (REST API 에러 응답)
            roomEventWaitManager.notifyFailure(event.eventId(), e);
        }
    }
}
