package coffeeshout.room.infra.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomPersistenceService {

    private final RoomJpaRepository roomJpaRepository;

    public void saveRoomSession(String joinCodeValue) {
        final RoomEntity roomEntity = new RoomEntity(joinCodeValue);
        roomJpaRepository.save(roomEntity);
        log.info("RoomEntity 저장 완료: joinCode={}", joinCodeValue);
    }
}
