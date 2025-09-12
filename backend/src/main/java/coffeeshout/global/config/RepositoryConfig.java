package coffeeshout.global.config;

import coffeeshout.room.domain.repository.MemoryRoomRepository;
import coffeeshout.room.domain.repository.RedisRoomRepository;
import coffeeshout.room.domain.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@RequiredArgsConstructor
public class RepositoryConfig {

    private final RedisTemplate<String, Object> redisTemplate;

    @Bean
    @Primary
    @ConditionalOnProperty(name = "repository.type", havingValue = "redis", matchIfMissing = true)
    public RoomRepository redisRoomRepository() {
        return new RedisRoomRepository(redisTemplate);
    }

    @Bean
    @ConditionalOnProperty(name = "repository.type", havingValue = "memory")
    public RoomRepository memoryRoomRepository() {
        return new MemoryRoomRepository();
    }
}
