package coffeeshout.room.domain.repository;

import static org.springframework.util.Assert.notNull;

import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisRoomRepository implements RoomRepository {

    private static final String ROOM_KEY = "room:%s";
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Optional<Room> findByJoinCode(JoinCode joinCode) {
        final Object room = redisTemplate.opsForHash().get(String.format(ROOM_KEY, joinCode.getValue()), "info");
        if(room == null) {
            return Optional.empty();
        }
        if(!(room instanceof Room)) {
            throw new IllegalStateException("저장된 객체의 타입이 Room이 아닙니다.");
        }

        return Optional.ofNullable((Room) room);
    }

    @Override
    public boolean existsByJoinCode(JoinCode joinCode) {
        return redisTemplate.opsForHash().hasKey(String.format(ROOM_KEY, joinCode.getValue()), "info");
    }

    @Override
    public Room save(Room room) {
        redisTemplate.opsForHash().put(String.format(ROOM_KEY, room.getJoinCode().getValue()), "info", room);
        return room;
    }

    @Override
    public void deleteByJoinCode(JoinCode joinCode) {
        notNull(joinCode, "JoinCode는 null일 수 없습니다.");
        redisTemplate.opsForHash().delete(String.format(ROOM_KEY, joinCode.getValue()), "info");
    }
}
