package coffeeshout.global.config;

import coffeeshout.global.config.properties.RedisProperties;
import coffeeshout.global.redis.listener.MiniGameSyncListener;
import coffeeshout.global.redis.listener.PlayerSyncListener;
import coffeeshout.global.redis.listener.RoomSyncListener;
import coffeeshout.global.redis.listener.RouletteSyncListener;
import coffeeshout.global.redis.listener.WebSocketSyncListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    private final RedisProperties redisProperties;

    public RedisConfig(RedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

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
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            RoomSyncListener roomSyncListener,
            PlayerSyncListener playerSyncListener,
            MiniGameSyncListener miniGameSyncListener,
            RouletteSyncListener rouletteSyncListener,
            WebSocketSyncListener webSocketSyncListener
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        // Room 관련 채널 구독
        container.addMessageListener(roomSyncListener, new PatternTopic("room:created"));
        container.addMessageListener(roomSyncListener, new PatternTopic("room:deleted"));
        container.addMessageListener(roomSyncListener, new PatternTopic("room:state"));

        // Player 관련 채널 구독
        container.addMessageListener(playerSyncListener, new PatternTopic("player:joined"));
        container.addMessageListener(playerSyncListener, new PatternTopic("player:removed"));
        container.addMessageListener(playerSyncListener, new PatternTopic("player:menu"));
        container.addMessageListener(playerSyncListener, new PatternTopic("player:ready"));
        container.addMessageListener(playerSyncListener, new PatternTopic("player:host"));

        // MiniGame 관련 채널 구독
        container.addMessageListener(miniGameSyncListener, new PatternTopic("minigame:updated"));
        container.addMessageListener(miniGameSyncListener, new PatternTopic("minigame:started"));

        // Roulette 관련 채널 구독
        container.addMessageListener(rouletteSyncListener, new PatternTopic("roulette:spin"));

        // WebSocket 관련 채널 구독
        container.addMessageListener(webSocketSyncListener, new PatternTopic("websocket:broadcast"));

        return container;
    }
}
