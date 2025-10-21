package coffeeshout.room.domain.repository;

import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.player.PlayerName;
import java.util.Optional;

public interface RoomRepository {

    Optional<Room> findByJoinCode(JoinCode joinCode);

    boolean existsByJoinCode(JoinCode joinCode);

    Room save(Room room);

    void deleteByJoinCode(JoinCode joinCode);
    
    // 부분 업데이트용 메서드들
    void updatePlayerReadyState(JoinCode joinCode, PlayerName playerName, Boolean isReady);
    
    void updatePlayers(JoinCode joinCode, Room room);
    
    void updateRoomState(JoinCode joinCode, String state);
    
    void updateMiniGames(JoinCode joinCode, Room room);
}
