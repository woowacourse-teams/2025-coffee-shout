package coffeeshout.room.infra.messaging;

import coffeeshout.global.exception.custom.InvalidArgumentException;
import coffeeshout.global.exception.custom.InvalidStateException;
import coffeeshout.global.infra.messaging.StreamEventHandler;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.event.RoomJoinEvent;
import coffeeshout.room.domain.menu.Menu;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.service.MenuCommandService;
import coffeeshout.room.domain.service.RoomCommandService;
import coffeeshout.room.ui.request.SelectedMenuRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 방 입장 이벤트를 처리하는 Handler
 * <p>
 * {@link RoomJoinEvent}를 처리하여 게스트를 방에 입장시키는 비즈니스 로직을 담당합니다.
 * 메시징 인프라로부터 분리되어 독립적으로 테스트 가능하며, 재사용 가능합니다.
 * </p>
 *
 * <p><b>책임:</b></p>
 * <ul>
 *   <li>메뉴 정보를 도메인 객체로 변환</li>
 *   <li>게스트를 방에 입장 처리</li>
 *   <li>비동기 응답 처리 (RoomEventWaitManager)</li>
 *   <li>비즈니스 예외 처리 및 실패 통지</li>
 * </ul>
 *
 * @see RoomJoinEvent
 * @see StreamEventHandler
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoomJoinEventHandler implements StreamEventHandler<RoomJoinEvent> {

    private final RoomCommandService roomCommandService;
    private final MenuCommandService menuCommandService;
    private final RoomEventWaitManager roomEventWaitManager;

    /**
     * 방 입장 이벤트를 처리합니다.
     * <p>
     * 게스트의 메뉴 선택 정보를 변환하고 방에 입장시킵니다.
     * 성공 시 {@link RoomEventWaitManager}를 통해 응답을 전달하고,
     * 실패 시 예외와 함께 실패를 통지합니다.
     * </p>
     *
     * @param event 방 입장 이벤트
     * @throws InvalidArgumentException 잘못된 입력값 (메뉴 ID, 게스트 이름 등)
     * @throws InvalidStateException    잘못된 상태 (방이 가득 참, 이미 게임 시작됨 등)
     */
    @Override
    public void handle(RoomJoinEvent event) {
        log.info("방 입장 이벤트 처리 시작: eventId={}, joinCode={}, guestName={}",
                event.eventId(), event.joinCode(), event.guestName());

        try {
            // 1. 메뉴 정보를 도메인 객체로 변환
            final SelectedMenuRequest selectedMenuRequest = event.selectedMenuRequest();
            final Menu menu = menuCommandService.convertMenu(
                    selectedMenuRequest.id(),
                    selectedMenuRequest.customName()
            );

            // 2. 게스트를 방에 입장 처리
            final Room room = roomCommandService.joinGuest(
                    new JoinCode(event.joinCode()),
                    new PlayerName(event.guestName()),
                    menu,
                    selectedMenuRequest.temperature()
            );

            log.info("방 입장 성공: joinCode={}, guestName={}, 현재 인원={}, eventId={}",
                    event.joinCode(), event.guestName(), room.getPlayers().size(), event.eventId());

            // 3. 비동기 대기 중인 요청에 성공 응답 전달
            roomEventWaitManager.notifySuccess(event.eventId(), room);

        } catch (InvalidArgumentException | InvalidStateException e) {
            log.warn("방 입장 처리 중 비즈니스 오류: joinCode={}, guestName={}, eventId={}, error={}",
                    event.joinCode(), event.guestName(), event.eventId(), e.getMessage());

            // 비즈니스 예외 발생 시 실패 통지
            roomEventWaitManager.notifyFailure(event.eventId(), e);
            throw e;

        } catch (Exception e) {
            log.error("방 입장 처리 중 시스템 오류: joinCode={}, guestName={}, eventId={}",
                    event.joinCode(), event.guestName(), event.eventId(), e);

            // 시스템 예외 발생 시 실패 통지
            roomEventWaitManager.notifyFailure(event.eventId(), e);
            throw e;
        }
    }
}
