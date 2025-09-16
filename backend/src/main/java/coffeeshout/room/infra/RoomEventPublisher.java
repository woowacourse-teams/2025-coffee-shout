package coffeeshout.room.infra;

import coffeeshout.room.domain.event.RoomCreateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomEventPublisher {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic roomEventTopic;
    
    public void publishRoomCreateEvent(RoomCreateEvent event) {
        try {
            redisTemplate.convertAndSend(roomEventTopic.getTopic(), event);
            log.info("방 생성 이벤트 발행됨: eventId={}, hostName={}", event.getEventId(), event.getHostName());
        } catch (Exception e) {
            log.error("방 생성 이벤트 발행 실패: eventId={}", event.getEventId(), e);
        }
    }
}
