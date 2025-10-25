package coffeeshout.room.infra.messaging;

import coffeeshout.global.redis.AbstractRedisListener;
import coffeeshout.global.redis.EventHandlerMapping;
import coffeeshout.global.trace.TracerProvider;
import coffeeshout.global.websocket.LoggingSimpMessagingTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RoomRedisSubscriber extends AbstractRedisListener {

    public RoomRedisSubscriber(
            LoggingSimpMessagingTemplate loggingSimpMessagingTemplate,
            ObjectMapper objectMapper,
            RedisMessageListenerContainer redisMessageListenerContainer,
            ChannelTopic roomEventTopic,
            EventHandlerMapping handlerFactory,
            TracerProvider tracerProvider
    ) {
        super(
                loggingSimpMessagingTemplate,
                objectMapper,
                redisMessageListenerContainer,
                roomEventTopic,
                handlerFactory,
                tracerProvider
        );
    }
}
