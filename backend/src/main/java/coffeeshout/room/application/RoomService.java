package coffeeshout.room.application;

import coffeeshout.player.domain.Menu;
import coffeeshout.player.domain.MenuFinder;
import coffeeshout.player.domain.Player;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.JoinCodeGenerator;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final MenuFinder menuFinder;
    private final JoinCodeGenerator joinCodeGenerator;

    public Room createRoom(String hostName, Long menuId) {
        final Menu menu = menuFinder.findById(menuId);
        final Player host = new Player(hostName, menu);

        final JoinCode joinCode = joinCodeGenerator.generate();
        final Room room = new Room(joinCode, host);

        return roomRepository.save(room);
    }
}
