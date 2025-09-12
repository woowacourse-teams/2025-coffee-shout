package coffeeshout.global.config.redis;

import coffeeshout.global.config.redis.dto.RoomDto;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/redis-test")
@RequiredArgsConstructor
public class RedisTestController {

    private final RedisTemplate<String, RoomDto> roomDtoRedisTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RoomRepository roomRepository;

    /**
     * 현재 사용 중인 Repository 타입 확인
     */
    @GetMapping("/repository-info")
    public Map<String, Object> getRepositoryInfo() {
        Map<String, Object> result = new HashMap<>();
        result.put("repositoryType", roomRepository.getClass().getSimpleName());
        result.put("repositoryClass", roomRepository.getClass().getName());
        
        try {
            redisTemplate.opsForValue().set("test:ping", "pong");
            redisTemplate.delete("test:ping");
            result.put("redisConnected", true);
        } catch (Exception e) {
            result.put("redisConnected", false);
            result.put("redisError", e.getMessage());
        }
        
        return result;
    }

    /**
     * Redis에 저장된 모든 Room 키 조회
     */
    @GetMapping("/rooms")
    public Map<String, Object> getAllRoomKeys() {
        Set<String> roomKeys = roomDtoRedisTemplate.keys("room:*");
        
        Map<String, Object> result = new HashMap<>();
        result.put("roomKeys", roomKeys);
        result.put("roomCount", roomKeys != null ? roomKeys.size() : 0);
        return result;
    }

    /**
     * 특정 JoinCode의 Room 데이터 조회
     */
    @GetMapping("/room/{joinCode}")
    public Map<String, Object> getRoomData(@PathVariable String joinCode) {
        JoinCode code = new JoinCode(joinCode);
        Optional<Room> room = roomRepository.findByJoinCode(code);
        
        Map<String, Object> result = new HashMap<>();
        result.put("joinCode", joinCode);
        result.put("found", room.isPresent());
        
        if (room.isPresent()) {
            Room r = room.get();
            result.put("playerCount", r.getPlayers().size());
            result.put("roomState", r.getRoomState().toString());
            result.put("playerNames", r.getPlayers().stream()
                    .map(p -> p.getName().value())
                    .toList());
        }
        
        return result;
    }

    /**
     * Redis 연결 상태 확인
     */
    @GetMapping("/health")
    public Map<String, Object> checkRedisHealth() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            redisTemplate.opsForValue().set("test:ping", "pong");
            String pong = (String) redisTemplate.opsForValue().get("test:ping");
            redisTemplate.delete("test:ping");
            
            result.put("status", "OK");
            result.put("ping", pong);
            result.put("connected", true);
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
            result.put("connected", false);
        }
        
        return result;
    }

    /**
     * 특정 Room의 TTL 확인
     */
    @GetMapping("/ttl/{joinCode}")
    public Map<String, Object> getRoomTtl(@PathVariable String joinCode) {
        String roomKey = "room:" + joinCode;
        
        Long roomTtl = roomDtoRedisTemplate.getExpire(roomKey);
        
        Map<String, Object> result = new HashMap<>();
        result.put("joinCode", joinCode);
        result.put("roomTtlSeconds", roomTtl);
        result.put("roomTtlHours", roomTtl != null ? roomTtl / 3600.0 : null);
        
        return result;
    }
}
