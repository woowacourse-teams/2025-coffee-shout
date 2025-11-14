package coffeeshout.room.infra.messaging;

import coffeeshout.global.config.properties.RedisStreamProperties;
import coffeeshout.global.infra.messaging.RedisStreamPublisher;
import coffeeshout.room.domain.event.RoomJoinEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomEnterStreamProducer {

    private final RedisStreamPublisher redisStreamPublisher;
    private final RedisStreamProperties redisStreamProperties;

    public void broadcastEnterRoom(RoomJoinEvent event) {
        log.debug("방 입장 이벤트 발송 준비: eventId={}, joinCode={}, guestName={}",
                event.eventId(), event.joinCode(), event.guestName());

        redisStreamPublisher.publish(
                redisStreamProperties.roomJoinKey(),
                event,
                "방 입장 이벤트"
        );

        log.debug("방 입장 이벤트 발송 완료: eventId={}", event.eventId());
    }
}
