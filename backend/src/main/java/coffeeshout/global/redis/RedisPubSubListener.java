package coffeeshout.global.redis;

import coffeeshout.global.redis.stream.EventHandlerFacade;
import coffeeshout.global.trace.Traceable;
import coffeeshout.global.trace.TracerProvider;
import coffeeshout.global.websocket.LoggingSimpMessagingTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class RedisPubSubListener implements MessageListener {

    private final ObjectMapper objectMapper;
    private final RedisMessageListenerContainer redisMessageListenerContainer;
    private final ChannelTopic roomEventTopic;
    private final EventHandlerFacade eventHandlerFacade;

    @PostConstruct
    public void subscribe() {
        redisMessageListenerContainer.addMessageListener(this, roomEventTopic);
        log.info("방 이벤트 구독 시작: topic={}", roomEventTopic.getTopic());
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        eventHandlerFacade.handle(convertEvent(message));
    }

    private BaseEvent convertEvent(Message message) {
        try {
            return objectMapper.readValue(new String(message.getBody()), BaseEvent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
