package coffeeshout.global.config.redis;

import java.time.Duration;
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
    public StreamMessageListenerContainer<String, ObjectRecord<String, String>> orderedStreamMessageListenerContainer(
            RedisConnectionFactory redisConnectionFactory) {
        StreamMessageListenerContainerOptions<String, ObjectRecord<String, String>> options = StreamMessageListenerContainerOptions
                .builder()
                .batchSize(10)
                .executor(orderedThreadExecutor())
                .pollTimeout(Duration.ofSeconds(2))
                .targetType(String.class)
                .build();

        StreamMessageListenerContainer<String, ObjectRecord<String, String>> container = StreamMessageListenerContainer.create(
                redisConnectionFactory, options);

        container.start();
        return container;
    }

    @Bean
    public StreamMessageListenerContainer<String, ObjectRecord<String, String>> concurrentStreamMessageListenerContainer(
            RedisConnectionFactory redisConnectionFactory) {
        StreamMessageListenerContainerOptions<String, ObjectRecord<String, String>> options = StreamMessageListenerContainerOptions
                .builder()
                .batchSize(10)
                .executor(multiThreadExecutor())
                .pollTimeout(Duration.ofSeconds(2))
                .targetType(String.class)
                .build();

        StreamMessageListenerContainer<String, ObjectRecord<String, String>> container = StreamMessageListenerContainer.create(
                redisConnectionFactory, options);

        container.start();
        return container;
    }

    private ThreadPoolTaskExecutor orderedThreadExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();

        ex.setCorePoolSize(2); // 한 개가 아닌 두 개가 필요로 함. 하나는 폴링 작업, 하나는 메시지 처리를 담당하게 됨
        ex.setMaxPoolSize(2);
        ex.setQueueCapacity(100);
        ex.setThreadNamePrefix("redis-ordered-");
        ex.setWaitForTasksToCompleteOnShutdown(true);
        ex.setAwaitTerminationSeconds(10);
        ex.initialize();

        return ex;
    }

    private ThreadPoolTaskExecutor multiThreadExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();

        ex.setCorePoolSize(4);
        ex.setMaxPoolSize(8);
        ex.setQueueCapacity(100);
        ex.setThreadNamePrefix("redis-concurrent-");
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
