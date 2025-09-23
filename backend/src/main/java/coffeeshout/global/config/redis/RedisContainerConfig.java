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
    public ThreadPoolTaskExecutor redisStreamTaskExecutor() {
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

    @Bean
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer(
            RedisConnectionFactory redisConnectionFactory) {
        StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options = StreamMessageListenerContainerOptions
                .builder()
                .batchSize(10) // 한 번에 처리할 수를 한 개로 두어 동시성 문제 해소
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
    public StreamMessageListenerContainer<String, ObjectRecord<String, String>> selectCardCommandEventStreamMessageListenerContainer(
            RedisConnectionFactory redisConnectionFactory) {
        StreamMessageListenerContainerOptions<String, ObjectRecord<String, String>> options = StreamMessageListenerContainerOptions
                .builder()
                .batchSize(1) // 한 번에 처리할 수를 한 개로 두어 동시성 문제 해소
                .executor(redisStreamTaskExecutor())
                .pollTimeout(Duration.ofSeconds(2))
                .targetType(String.class)
                .build();

        StreamMessageListenerContainer<String, ObjectRecord<String, String>> container = StreamMessageListenerContainer.create(
                redisConnectionFactory, options);

        container.start();
        return container;
    }
}
