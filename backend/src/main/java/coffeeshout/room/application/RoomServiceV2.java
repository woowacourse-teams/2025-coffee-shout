package coffeeshout.room.application;

import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.menu.Menu;
import coffeeshout.room.domain.menu.SelectedMenu;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.service.JoinCodeGenerator;
import coffeeshout.room.domain.service.MenuCommandService;
import coffeeshout.room.domain.service.MenuQueryService;
import coffeeshout.room.domain.service.RoomCommandService;
import coffeeshout.room.domain.service.RoomQueryService;
import coffeeshout.room.ui.request.SelectedMenuRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomServiceV2 {

    private final RoomQueryService roomQueryService;
    private final RoomCommandService roomCommandService;
    private final JoinCodeGenerator joinCodeGenerator;
    private final MenuQueryService menuQueryService;
    private final MenuCommandService menuCommandService;
    private final QrCodeService qrCodeService;
    private final DelayedRoomRemovalService delayedRoomRemovalService;

    public Room create(String hostName, SelectedMenuRequest selectedMenuRequest) {
        // 방 생성의 경우에는 먼저 처리하고 이벤트를 발행한다.
        final Menu menu = menuCommandService.convertMenu(selectedMenuRequest.id(), selectedMenuRequest.customName());

        // 조인코드를 생성할 때 중복이 발생할 수 있으므로 Redis에 조인코드를 보관해둔다.
        // Redis는 원자성을 보장하기 때문에 중복된 조인코드가 생성되는 것을 방지할 수 있다.
        final JoinCode joinCode = joinCodeGenerator.generate();

        // Room이 생성되다가 오류가 발생하면 JoinCode가 남아있을 수 있다. 이에 대한 TTL이나 처리 로직이 필요로 한다.
        final Room room = Room.createNewRoom(
                joinCode,
                new PlayerName(hostName),
                new SelectedMenu(menu, selectedMenuRequest.temperature())
        );

        assignQrCodeUrl(room);
        scheduleRemoveRoom(joinCode);

        // TODO 이벤트 발행

        return roomCommandService.save(room);
    }

    public Room enterRoom(String joinCode, String playerName, SelectedMenuRequest selectedMenuRequest) {
        // 먼저 각 인스턴스의 인원이 들어와 충돌이 날 수 있다.
        // Stream 을 통해서 처리할 필요가 있다.

        // 방법 1. Redis Stream으로 작업 처리를 블로킹에서 기다린 후 처리한다.
        // 방법 2.  Controller에서 상관없이 QRCodeUrl를 반환하고 이 때 중앙제어장치인 Redis Players(Set)구조에 넣는다.
        // Stomp Subscribe 단계에서 해당 Redis Players(Set)구조에 없는 이름이라면 요청을 거부한다.
        // 이름 똑같은 사용자가 가로채면? => 우리는 보안은 신경쓰지 않는다.
        // 방법 3.

        return null;
    }

    public List<Player> getPlayers(String joinCode) {
        final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));

        return room.getPlayers();
    }

    public List<Player> selectMenu(String joinCode, String playerName, Long menuId) {
        // 메뉴 변경은 강력한 일관성이 필요로 하지 않는다.
        // pub sub 처리 후 최종적 일관성 처리한다.

        // TODO: 구현 필요
        return List.of();
    }


    private void assignQrCodeUrl(Room room) {
        final String qrCodeUrl = qrCodeService.getQrCodeUrl(room.getJoinCode().getValue());
        room.assignQrCodeUrl(qrCodeUrl);
    }

    private void scheduleRemoveRoom(JoinCode joinCode) {
        try {
            delayedRoomRemovalService.scheduleRemoveRoom(joinCode);
        } catch (Exception e) {
            log.error("방 제거 스케줄링 실패: joinCode={}", joinCode.getValue(), e);
        }
    }
}
