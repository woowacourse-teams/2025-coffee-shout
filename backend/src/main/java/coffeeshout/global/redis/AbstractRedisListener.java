package coffeeshout.global.redis;

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

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractRedisListener implements MessageListener {

    private final LoggingSimpMessagingTemplate loggingSimpMessagingTemplate;
    private final ObjectMapper objectMapper;
    private final RedisMessageListenerContainer redisMessageListenerContainer;
    private final ChannelTopic roomEventTopic;
    private final EventHandlerMapping handlerFactory;
    private final TracerProvider tracerProvider;

    @PostConstruct
    public void subscribe() {
        redisMessageListenerContainer.addMessageListener(this, roomEventTopic);
        log.info("방 이벤트 구독 시작: topic={}", roomEventTopic.getTopic());
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        final BaseEvent event = convertEvent(message);
        handle(event);
    }

    private BaseEvent convertEvent(Message message) {
        try {
            return objectMapper.readValue(new String(message.getBody()), BaseEvent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void handle(BaseEvent event) {
        try {
            final EventHandler handler = handlerFactory.getHandler(event);
            final Runnable handling = () -> handler.handle(event);

            if (event instanceof Traceable traceable) {
                tracerProvider.executeWithTraceContext(traceable.traceInfo(), handling, event);
                return;
            }
            handling.run();
        } catch (Exception e) {
            log.error("이벤트 처리 실패: message={}", event, e);
            if (event instanceof UserEvent userEvent) {
                loggingSimpMessagingTemplate.convertAndSendError(userEvent.userName(), e);
            }
        }
    }
}
