package coffeeshout.global.config;

import coffeeshout.global.config.properties.RedisProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    private final RedisProperties redisProperties;


    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = 
            new RedisStandaloneConfiguration(redisProperties.host(), redisProperties.port());
        
        LettuceClientConfiguration.LettuceClientConfigurationBuilder clientConfig = 
            LettuceClientConfiguration.builder();
        
        if (redisProperties.ssl().enabled()) {
            clientConfig.useSsl();
        }
        
        return new LettuceConnectionFactory(redisStandaloneConfiguration, clientConfig.build());
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory redisConnectionFactory,
            ObjectMapper objectMapper
    ) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 문자열 키 직렬화
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());

        // 객체 값 직렬화 (JSON 형태로 저장)
        final GenericJackson2JsonRedisSerializer jackson2JsonRedisSerializer = new GenericJackson2JsonRedisSerializer(
                objectMapper);
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
        stringRedisTemplate.setConnectionFactory(redisConnectionFactory);
        return stringRedisTemplate;
    }

    @Bean
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer(
            RedisConnectionFactory redisConnectionFactory
    ) {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(4);
        taskExecutor.setMaxPoolSize(8);
        taskExecutor.setQueueCapacity(100);
        taskExecutor.setThreadNamePrefix("redis-stream-");
        taskExecutor.initialize();

        StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options = StreamMessageListenerContainerOptions
                .builder()
                .batchSize(1)
                .executor(taskExecutor)
                .pollTimeout(Duration.ofSeconds(1))
                .build();

        StreamMessageListenerContainer<String, MapRecord<String, String, String>> container =
                StreamMessageListenerContainer.create(redisConnectionFactory, options);

        container.start();
        return container;
    }
}
