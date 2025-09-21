package coffeeshout.room.domain.service;

import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.menu.Menu;
import coffeeshout.room.domain.menu.SelectedMenu;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.repository.RoomRepository;
import coffeeshout.room.ui.request.SelectedMenuRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class RoomCommandService {

    private final RoomRepository roomRepository;
    private final RoomQueryService roomQueryService;
    private final MenuCommandService menuCommandService;

    public Room save(Room room) {
        return roomRepository.save(room);
    }

    public void delete(@NonNull JoinCode joinCode) {
        roomRepository.deleteByJoinCode(joinCode);
    }

    public Room joinGuest(String joinCode, String guestName, SelectedMenuRequest selectedMenuRequest) {
        log.info("JoinCode[{}] 게스트 입장 - 게스트 이름: {}, 메뉴 정보: {}", joinCode, guestName, selectedMenuRequest);

        final Menu menu = menuCommandService.convertMenu(selectedMenuRequest.id(), selectedMenuRequest.customName());
        final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));

        room.joinGuest(new PlayerName(guestName), new SelectedMenu(menu, selectedMenuRequest.temperature()));

        return save(room);
    }
}
