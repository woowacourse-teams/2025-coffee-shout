package coffeeshout.room.domain.service;

import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.QrCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.menu.Menu;
import coffeeshout.room.domain.menu.MenuTemperature;
import coffeeshout.room.domain.menu.SelectedMenu;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.repository.RoomRepository;
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

    public Room save(Room room) {
        return roomRepository.save(room);
    }

    public void delete(@NonNull JoinCode joinCode) {
        roomRepository.deleteByJoinCode(joinCode);
    }

    public Room joinGuest(JoinCode joinCode, PlayerName playerName, Menu menu, MenuTemperature menuTemperature) {
        log.info("JoinCode[{}] 게스트 입장 - 게스트 이름: {}, 메뉴 정보: {}, 온도 : {} ", joinCode, playerName, menu, menuTemperature);
        final Room room = roomQueryService.getByJoinCode(joinCode);

        room.joinGuest(playerName, new SelectedMenu(menu, menuTemperature));

        return save(room);
    }

    public Room saveIfAbsentRoom(JoinCode joinCode, PlayerName hostName, Menu menu, MenuTemperature menuTemperature) {
        if (roomRepository.existsByJoinCode(joinCode)) {
            log.warn("JoinCode[{}] 방 생성 실패 - 이미 존재하는 방", joinCode);
            return roomQueryService.getByJoinCode(joinCode);
        }

        log.info("JoinCode[{}] 방 생성 - 호스트 이름: {}, 메뉴 정보: {}, 온도 : {} ", joinCode, hostName, menu, menuTemperature);

        final Room room = Room.createNewRoom(joinCode, hostName, new SelectedMenu(menu, menuTemperature));

        return save(room);
    }

    public void assignQrCode(JoinCode joinCode, String qrCodeUrl) {
        updateRoomWithQrCode(joinCode, room -> room.assignQrCode(QrCode.success(qrCodeUrl)));
    }

    public void assignQrCodeError(JoinCode joinCode) {
        updateRoomWithQrCode(joinCode, room -> room.assignQrCode(QrCode.error()));
    }

    private void updateRoomWithQrCode(JoinCode joinCode, java.util.function.Consumer<Room> qrCodeAssigner) {
        Room room = roomQueryService.getByJoinCode(joinCode);
        qrCodeAssigner.accept(room);
        save(room);
    }
}
