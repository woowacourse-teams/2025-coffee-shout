package coffeeshout.global.config.redis;

import coffeeshout.global.config.redis.dto.RoomDto;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

import static org.springframework.util.Assert.notNull;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisRoomRepository implements RoomRepository {

    private static final String ROOM_KEY_PREFIX = "room:";
    private static final Duration ROOM_TTL = Duration.ofHours(24); // 24시간 후 자동 삭제
    
    private final RedisTemplate<String, RoomDto> roomDtoRedisTemplate;
    private final RoomDtoConverter roomDtoConverter;

    @Override
    public Optional<Room> findByJoinCode(JoinCode joinCode) {
        notNull(joinCode, "JoinCode는 null일 수 없습니다.");
        
        String key = ROOM_KEY_PREFIX + joinCode.getValue();
        RoomDto roomDto = roomDtoRedisTemplate.opsForValue().get(key);
        
        if (roomDto == null) {
            log.debug("Redis에서 방 조회 실패: joinCode={}", joinCode.getValue());
            return Optional.empty();
        }

        try {
            Room room = roomDtoConverter.toRoom(roomDto);
            log.debug("Redis에서 방 조회 성공: joinCode={}, players={}", 
                    joinCode.getValue(), room.getPlayers().size());
            return Optional.of(room);
        } catch (Exception e) {
            log.error("RoomDto -> Room 변환 실패: joinCode={}", joinCode.getValue(), e);
            return Optional.empty();
        }
    }

    @Override
    public boolean existsByJoinCode(JoinCode joinCode) {
        notNull(joinCode, "JoinCode는 null일 수 없습니다.");
        
        String key = ROOM_KEY_PREFIX + joinCode.getValue();
        Boolean exists = roomDtoRedisTemplate.hasKey(key);
        
        log.debug("Redis에서 방 존재 확인: joinCode={}, exists={}", joinCode.getValue(), exists);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public Room save(Room room) {
        notNull(room, "Room은 null일 수 없습니다.");
        notNull(room.getJoinCode(), "JoinCode는 null일 수 없습니다.");
        
        try {
            String roomKey = ROOM_KEY_PREFIX + room.getJoinCode().getValue();
            RoomDto roomDto = roomDtoConverter.toDto(room);
            
            // RoomDto 저장 (TTL 설정)
            roomDtoRedisTemplate.opsForValue().set(roomKey, roomDto, ROOM_TTL);
            
            log.info("Redis에 방 저장 성공: joinCode={}, players={}", 
                    room.getJoinCode().getValue(), 
                    room.getPlayers().size());
                    
            return room;
            
        } catch (Exception e) {
            log.error("Room -> RoomDto 변환 또는 저장 실패: joinCode={}", 
                    room.getJoinCode().getValue(), e);
            throw new RuntimeException("Redis 저장 실패", e);
        }
    }

    @Override
    public void deleteByJoinCode(JoinCode joinCode) {
        notNull(joinCode, "JoinCode는 null일 수 없습니다.");

        String roomKey = ROOM_KEY_PREFIX + joinCode.getValue();
        
        Boolean deleted = roomDtoRedisTemplate.delete(roomKey);
        
        log.info("Redis에서 방 삭제: joinCode={}, deleted={}", joinCode.getValue(), deleted);
    }

    /**
     * 모든 활성 방 개수 조회
     */
    public long countActiveRooms() {
        Set<String> keys = roomDtoRedisTemplate.keys(ROOM_KEY_PREFIX + "*");
        long count = keys != null ? keys.size() : 0;
        log.debug("활성 방 개수: {}", count);
        return count;
    }

    /**
     * Room TTL 연장 (게임 진행 중일 때 사용)
     */
    public void extendRoomTtl(JoinCode joinCode, Duration newTtl) {
        notNull(joinCode, "JoinCode는 null일 수 없습니다.");
        notNull(newTtl, "TTL은 null일 수 없습니다.");
        
        String roomKey = ROOM_KEY_PREFIX + joinCode.getValue();
        
        Boolean result = roomDtoRedisTemplate.expire(roomKey, newTtl);
        
        log.debug("Room TTL 연장: joinCode={}, newTtl={}분, success={}", 
                joinCode.getValue(), newTtl.toMinutes(), result);
    }

    /**
     * Room 강제 새로고침 (Redis에서 최신 데이터 조회)
     */
    public Optional<Room> refreshRoom(JoinCode joinCode) {
        log.debug("Room 강제 새로고침: joinCode={}", joinCode.getValue());
        return findByJoinCode(joinCode);
    }
}
