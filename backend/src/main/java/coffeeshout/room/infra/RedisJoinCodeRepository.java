package coffeeshout.room.infra;

import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.repository.JoinCodeRepository;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisJoinCodeRepository implements JoinCodeRepository {

    private static final String JOIN_CODE_KEY_PREFIX = "room:joinCode:";

    @Value("${room.removalDelay}")
    private Duration ttl;

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean existsByJoinCode(JoinCode joinCode) {
        final String key = JOIN_CODE_KEY_PREFIX + joinCode.getValue();

        // null이 발생할 수 있으므로 다음과 같이 처리해야함
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public JoinCode save(JoinCode joinCode) {
        final String key = JOIN_CODE_KEY_PREFIX + joinCode.getValue();
        // value는 간단히 "1"로 저장 (존재 여부만 확인하면 되므로)
        redisTemplate.opsForValue().set(key, "1", ttl);
        return joinCode;
    }
}
