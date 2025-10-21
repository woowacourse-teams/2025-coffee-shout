package coffeeshout.room.domain.service;

import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.repository.RoomRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomCommandService {

    private final RoomRepository roomRepository;

    public Room save(Room room) {
        return roomRepository.save(room);
    }

    public void delete(@NonNull JoinCode joinCode) {
        roomRepository.deleteByJoinCode(joinCode);
    }
    
    // 부분 업데이트 메서드들
    public void updatePlayerReadyState(JoinCode joinCode, PlayerName playerName, Boolean isReady) {
        roomRepository.updatePlayerReadyState(joinCode, playerName, isReady);
    }
    
    public void updatePlayers(JoinCode joinCode, Room room) {
        roomRepository.updatePlayers(joinCode, room);
    }
    
    public void updateRoomState(JoinCode joinCode, String state) {
        roomRepository.updateRoomState(joinCode, state);
    }
    
    public void updateMiniGames(JoinCode joinCode, Room room) {
        roomRepository.updateMiniGames(joinCode, room);
    }
}
