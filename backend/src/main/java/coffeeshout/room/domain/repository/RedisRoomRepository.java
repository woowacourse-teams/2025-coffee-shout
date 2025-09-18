package coffeeshout.room.domain.repository;

import static org.springframework.util.Assert.notNull;

import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisRoomRepository implements RoomRepository {

    private static final String ROOM_KEY = "room:%s";
    private static final Duration ROOM_DEFAULT_TTL = Duration.ofHours(1);
    private static final String FIELD_INFO = "info";

    private final RedisTemplate<String, Object> redisTemplate;


    @Override
    public Optional<Room> findByJoinCode(JoinCode joinCode) {
        final Object room = redisTemplate.opsForHash().get(String.format(ROOM_KEY, joinCode.getValue()), FIELD_INFO);
        if (room == null) {
            return Optional.empty();
        }
        if (!(room instanceof Room)) {
            throw new IllegalStateException("저장된 객체의 타입이 Room이 아닙니다.");
        }

        return Optional.ofNullable((Room) room);
    }

    @Override
    public boolean existsByJoinCode(JoinCode joinCode) {
        return redisTemplate.opsForHash().hasKey(String.format(ROOM_KEY, joinCode.getValue()), FIELD_INFO);
    }

    @Override
    public Room save(Room room) {
        redisTemplate.opsForHash().put(createRedisKey(room.getJoinCode()), FIELD_INFO, room);
        redisTemplate.expire(String.format(ROOM_KEY, room.getJoinCode().getValue()), ROOM_DEFAULT_TTL);
        return room;
    }

    @Override
    public void deleteByJoinCode(JoinCode joinCode) {
        notNull(joinCode, "JoinCode는 null일 수 없습니다.");
        redisTemplate.opsForHash().delete(String.format(ROOM_KEY, joinCode.getValue()), FIELD_INFO);
    }

    private String createRedisKey(JoinCode joinCode) {
        return String.format(ROOM_KEY, joinCode.getValue());
    }
}
