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

    private static final String JOIN_CODE_SET_KEY = "room:joinCodes";

    @Value("${room.removalDelay}")
    private Duration ttl;

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean existsByJoinCode(JoinCode joinCode) {
        Boolean isMember = redisTemplate.opsForSet().isMember(JOIN_CODE_SET_KEY, joinCode.getValue());

        // null이 발생할 수 있으므로 다음과 같이 처리해야함
        return Boolean.TRUE.equals(isMember);
    }

    @Override
    public void save(JoinCode joinCode) {
        redisTemplate.opsForSet().add(JOIN_CODE_SET_KEY, joinCode.getValue());
        redisTemplate.expire(JOIN_CODE_SET_KEY, ttl);
    }
}
