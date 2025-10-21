package coffeeshout.room.domain.repository;

import static org.springframework.util.Assert.notNull;

import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.player.PlayerName;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

//@Repository
public class MemoryRoomRepository implements RoomRepository {

    private final Map<JoinCode, Room> rooms;

    public MemoryRoomRepository() {
        this.rooms = new ConcurrentHashMap<>();
    }

    @Override
    public Optional<Room> findByJoinCode(JoinCode joinCode) {
        return Optional.ofNullable(rooms.get(joinCode));
    }

    @Override
    public boolean existsByJoinCode(JoinCode joinCode) {
        return rooms.containsKey(joinCode);
    }

    @Override
    public Room save(Room room) {
        rooms.put(room.getJoinCode(), room);
        return rooms.get(room.getJoinCode());
    }

    @Override
    public void deleteByJoinCode(JoinCode joinCode) {
        notNull(joinCode, "JoinCode는 null일 수 없습니다.");
        rooms.remove(joinCode);
    }

    @Override
    public void updatePlayerReadyState(JoinCode joinCode, PlayerName playerName, Boolean isReady) {
        // 메모리 방식에서는 save()와 동일
        Room room = rooms.get(joinCode);
        if (room != null) {
            room.findPlayer(playerName).updateReadyState(isReady);
        }
    }

    @Override
    public void updatePlayers(JoinCode joinCode, Room room) {
        // 메모리 방식에서는 save()와 동일
        save(room);
    }

    @Override
    public void updateRoomState(JoinCode joinCode, String state) {
        // 메모리 방식에서는 객체 참조라 자동 반영됨
        // 별도 작업 불필요
    }

    @Override
    public void updateMiniGames(JoinCode joinCode, Room room) {
        // 메모리 방식에서는 save()와 동일
        save(room);
    }
}
