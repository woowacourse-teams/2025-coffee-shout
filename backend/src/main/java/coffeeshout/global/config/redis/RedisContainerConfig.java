package coffeeshout.global.config.redis;

import java.time.Duration;
import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class RedisContainerConfig {

    @Bean
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer(
            RedisConnectionFactory redisConnectionFactory) {
        StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options = StreamMessageListenerContainerOptions
                .builder()
                .batchSize(10)
                .executor(redisStreamTaskExecutor())
                .pollTimeout(Duration.ofSeconds(2))
                .serializer(new StringRedisSerializer())
                .build();

        StreamMessageListenerContainer<String, MapRecord<String, String, String>> container = StreamMessageListenerContainer.create(
                redisConnectionFactory, options);

        container.start();
        return container;
    }

    @Bean
    public StreamMessageListenerContainer<String, ObjectRecord<String, String>> roomEnterStreamContainer(
            RedisConnectionFactory redisConnectionFactory) {
        return getListenerContainer(redisConnectionFactory, roomEnterThreadExecutor());
    }

    @Bean
    public StreamMessageListenerContainer<String, ObjectRecord<String, String>> cardSelectStreamContainer(
            RedisConnectionFactory redisConnectionFactory) {
        return getListenerContainer(redisConnectionFactory, cardSelectThreadExecutor());
    }

    @Bean
    public StreamMessageListenerContainer<String, ObjectRecord<String, String>> concurrentStreamMessageListenerContainer(
            RedisConnectionFactory redisConnectionFactory) {
        return getListenerContainer(redisConnectionFactory, redisStreamTaskExecutor());
    }

    private StreamMessageListenerContainer<String, ObjectRecord<String, String>> getListenerContainer(
            RedisConnectionFactory redisConnectionFactory, Executor executor) {
        StreamMessageListenerContainerOptions<String, ObjectRecord<String, String>> options = StreamMessageListenerContainerOptions
                .builder()
                .batchSize(10) // 한 번에 처리할 메시지 수
                .executor(executor) // 쓰레드 설정
                .pollTimeout(Duration.ofSeconds(2)) // 폴링 주기
                .targetType(String.class)
                .build();

        StreamMessageListenerContainer<String, ObjectRecord<String, String>> container = StreamMessageListenerContainer.create(
                redisConnectionFactory, options);

        container.start();
        return container;
    }

    private ThreadPoolTaskExecutor roomEnterThreadExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();

        ex.setCorePoolSize(1); // 순서 보장을 위해 단일 스레드
        ex.setMaxPoolSize(1);
        ex.setQueueCapacity(100);
        ex.setThreadNamePrefix("redis-room-enter-");
        ex.setWaitForTasksToCompleteOnShutdown(true);
        ex.setAwaitTerminationSeconds(10);
        ex.initialize();

        return ex;
    }

    private ThreadPoolTaskExecutor cardSelectThreadExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();

        ex.setCorePoolSize(1); // 순서 보장을 위해 단일 스레드
        ex.setMaxPoolSize(1);
        ex.setQueueCapacity(100);
        ex.setThreadNamePrefix("redis-card-select-");
        ex.setWaitForTasksToCompleteOnShutdown(true);
        ex.setAwaitTerminationSeconds(10);
        ex.initialize();

        return ex;
    }

    private ThreadPoolTaskExecutor redisStreamTaskExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();

        ex.setCorePoolSize(4);
        ex.setMaxPoolSize(8);
        ex.setQueueCapacity(100);
        ex.setThreadNamePrefix("redis-stream-");
        ex.setWaitForTasksToCompleteOnShutdown(true);
        ex.setAwaitTerminationSeconds(10);
        ex.initialize();

        return ex;
    }
}
