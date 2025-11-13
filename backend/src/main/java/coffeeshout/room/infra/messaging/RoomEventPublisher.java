package coffeeshout.room.infra.messaging;

import coffeeshout.global.config.redis.EventTopicRegistry;
import coffeeshout.global.config.redis.TopicManager;
import coffeeshout.room.domain.event.RoomBaseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomEventPublisher {

    private final RedisTemplate<String, Object> redisTemplate;
    private final TopicManager topicManager;

    public <T extends RoomBaseEvent> void publishEvent(T event) {
        try {
            final String topic = topicManager.getTopic(EventTopicRegistry.ROOM).getTopic();
            redisTemplate.convertAndSend(topic, event);
            log.info("이벤트 발행됨: eventType={}, eventId={}",
                    event.eventType(), event.eventId());
        } catch (Exception e) {
            log.error("이벤트 발행 실패: eventType={}, eventId={}",
                    event.eventType(), event.eventId(), e);
            throw new RuntimeException("이벤트 발행 실패", e);
        }
    }
}
