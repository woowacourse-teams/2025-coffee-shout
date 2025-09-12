package coffeeshout.room.domain.repository;

import static org.springframework.util.Assert.notNull;

import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;

@RequiredArgsConstructor
public class RedisRoomRepository implements RoomRepository {

    private static final String ROOM_KEY_PREFIX = "room:";

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Optional<Room> findByJoinCode(JoinCode joinCode) {
        final String key = generateKey(joinCode);
        final Object room = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable((Room) room);
    }

    @Override
    public boolean existsByJoinCode(JoinCode joinCode) {
        final String key = generateKey(joinCode);
        return redisTemplate.hasKey(key);
    }

    @Override
    public Room save(Room room) {
        final String key = generateKey(room.getJoinCode());
        redisTemplate.opsForValue().set(key, room);
        return room;
    }

    @Override
    public void deleteByJoinCode(JoinCode joinCode) {
        notNull(joinCode, "JoinCode는 null일 수 없습니다.");

        final String key = generateKey(joinCode);
        redisTemplate.delete(key);
    }

    private String generateKey(JoinCode joinCode) {
        return ROOM_KEY_PREFIX + joinCode.getValue();
    }
}
