package coffeeshout.room.infra;

import static org.assertj.core.api.Assertions.assertThat;

import coffeeshout.global.ServiceTest;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.service.JoinCodeGenerator;
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

    private static final String JOIN_CODE_SET_KEY = "room:joinCodes";

    JoinCode joinCode;

    @BeforeEach
    void setUp() {
        joinCode = joinCodeGenerator.generate();
    }


    @AfterEach
    void tearDown() {
        // 각 테스트 후 Redis 데이터 정리
        redisTemplate.delete(JOIN_CODE_SET_KEY);
    }

    @Test
    void 조인코드를_저장한다() {
        // given & then
        redisJoinCodeRepository.save(joinCode);

        // then
        Boolean isMember = redisTemplate.opsForSet().isMember(JOIN_CODE_SET_KEY, joinCode.getValue());
        assertThat(isMember).isTrue();
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
        // given & then

        boolean exists = redisJoinCodeRepository.existsByJoinCode(joinCode);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    void 중복된_조인코드를_저장해도_Set에는_하나만_존재한다() {
        // given & then
        redisJoinCodeRepository.save(joinCode);
        redisJoinCodeRepository.save(joinCode);
        redisJoinCodeRepository.save(joinCode);

        // then
        Long size = redisTemplate.opsForSet().size(JOIN_CODE_SET_KEY);
        assertThat(size).isEqualTo(1);
    }

    @Test
    void 여러_조인코드를_저장할_수_있다() {
        // given
        JoinCode joinCode1 = new JoinCode("TEST4");
        JoinCode joinCode2 = new JoinCode("TEST5");
        JoinCode joinCode3 = new JoinCode("TEST6");

        // when
        redisJoinCodeRepository.save(joinCode1);
        redisJoinCodeRepository.save(joinCode2);
        redisJoinCodeRepository.save(joinCode3);

        // then
        assertThat(redisJoinCodeRepository.existsByJoinCode(joinCode1)).isTrue();
        assertThat(redisJoinCodeRepository.existsByJoinCode(joinCode2)).isTrue();
        assertThat(redisJoinCodeRepository.existsByJoinCode(joinCode3)).isTrue();

        Long size = redisTemplate.opsForSet().size(JOIN_CODE_SET_KEY);
        assertThat(size).isEqualTo(3);
    }

    @Test
    void 저장_시_TTL이_설정된다() {
        // given & then
        redisJoinCodeRepository.save(joinCode);

        // then
        Long ttl = redisTemplate.getExpire(JOIN_CODE_SET_KEY);
        assertThat(ttl).isNotNull()
                .isGreaterThan(0);
    }
}
