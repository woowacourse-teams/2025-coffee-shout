package coffeeshout.room.infra;

import static org.assertj.core.api.Assertions.assertThat;

import coffeeshout.global.ServiceTest;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.service.JoinCodeGenerator;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

class RedisJoinCodeRepositoryTest extends ServiceTest {

    @Autowired
    JoinCodeGenerator joinCodeGenerator;

    @Autowired
    RedisJoinCodeRepository redisJoinCodeRepository;

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    private static final String JOIN_CODE_KEY_PREFIX = "room:joinCode:";

    JoinCode joinCode;

    @BeforeEach
    void setUp() {
        joinCode = joinCodeGenerator.generate();
    }

    @AfterEach
    void tearDown() {
        // 각 테스트 후 Redis 데이터 정리 - 패턴 매칭으로 모든 조인코드 키 삭제
        Set<String> keys = redisTemplate.keys(JOIN_CODE_KEY_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Test
    void 조인코드를_저장한다() {
        // given & when
        redisJoinCodeRepository.save(joinCode);

        // then
        String key = JOIN_CODE_KEY_PREFIX + joinCode.getValue();
        Boolean hasKey = redisTemplate.hasKey(key);
        assertThat(hasKey).isTrue();
    }

    @Test
    void 저장된_조인코드가_존재하는지_확인한다() {
        // given
        redisJoinCodeRepository.save(joinCode);

        // when
        boolean exists = redisJoinCodeRepository.existsByJoinCode(joinCode);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void 저장되지_않은_조인코드는_존재하지_않는다() {
        // given & when
        boolean exists = redisJoinCodeRepository.existsByJoinCode(joinCode);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    void 여러_조인코드를_저장할_수_있다() {
        // given
        JoinCode joinCode1 = joinCodeGenerator.generate();
        JoinCode joinCode2 = joinCodeGenerator.generate();
        JoinCode joinCode3 = joinCodeGenerator.generate();

        // when
        redisJoinCodeRepository.save(joinCode1);
        redisJoinCodeRepository.save(joinCode2);
        redisJoinCodeRepository.save(joinCode3);

        // then
        assertThat(redisJoinCodeRepository.existsByJoinCode(joinCode1)).isTrue();
        assertThat(redisJoinCodeRepository.existsByJoinCode(joinCode2)).isTrue();
        assertThat(redisJoinCodeRepository.existsByJoinCode(joinCode3)).isTrue();

        Set<String> keys = redisTemplate.keys(JOIN_CODE_KEY_PREFIX + "*");
        assertThat(keys).hasSize(3);
    }

    @Test
    void 각_조인코드마다_개별_TTL이_설정된다() {
        // given
        JoinCode joinCode1 = joinCodeGenerator.generate();
        JoinCode joinCode2 = joinCodeGenerator.generate();

        // when
        redisJoinCodeRepository.save(joinCode1);
        redisJoinCodeRepository.save(joinCode2);

        // then - 각 키마다 TTL이 설정되어 있어야 함
        String key1 = JOIN_CODE_KEY_PREFIX + joinCode1.getValue();
        String key2 = JOIN_CODE_KEY_PREFIX + joinCode2.getValue();

        Long ttl1 = redisTemplate.getExpire(key1);
        Long ttl2 = redisTemplate.getExpire(key2);

        assertThat(ttl1).isNotNull().isGreaterThan(0);
        assertThat(ttl2).isNotNull().isGreaterThan(0);
    }

    @Test
    void 저장_시_TTL이_설정된다() {
        // given & when
        redisJoinCodeRepository.save(joinCode);

        // then
        String key = JOIN_CODE_KEY_PREFIX + joinCode.getValue();
        Long ttl = redisTemplate.getExpire(key);
        assertThat(ttl).isNotNull()
                .isGreaterThan(0);
    }
}
