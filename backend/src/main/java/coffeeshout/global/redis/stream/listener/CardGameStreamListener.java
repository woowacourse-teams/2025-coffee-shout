package coffeeshout.global.redis.stream.listener;

import coffeeshout.global.config.properties.RedisStreamProperties;
import coffeeshout.global.redis.stream.EventHandlerExecutor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
public class CardGameStreamListener extends AbstractStreamListener {

    private final RedisStreamProperties redisStreamProperties;

    public CardGameStreamListener(
            @Qualifier("cardSelectStreamContainer") StreamMessageListenerContainer<String, ObjectRecord<String, String>> streamContainer,
            ObjectMapper objectMapper,
            EventHandlerExecutor eventHandlerExecutor,
            RedisStreamProperties redisStreamProperties
    ) {
        super(streamContainer, objectMapper, eventHandlerExecutor);
        this.redisStreamProperties = redisStreamProperties;
    }

    @Override
    protected StreamOffset<String> configStreamOffset() {
        return StreamOffset.fromStart(redisStreamProperties.cardGameSelectKey());
    }
}
