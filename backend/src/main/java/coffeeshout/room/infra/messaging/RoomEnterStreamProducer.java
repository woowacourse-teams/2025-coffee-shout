package coffeeshout.room.infra.messaging;

import coffeeshout.global.config.properties.RedisStreamProperties;
import coffeeshout.global.infra.messaging.RedisStreamPublisher;
import coffeeshout.room.domain.event.RoomJoinEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Room 도메인 입장 이벤트를 Redis Stream으로 발행하는 Producer
 * <p>
 * {@link RedisStreamPublisher}를 합성(composition)하여 공통 발행 로직을 재사용합니다.
 * 이 클래스는 Room 도메인에 특화된 로깅과 Stream 키 설정을 담당합니다.
 * </p>
 *
 * @see RedisStreamPublisher
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoomEnterStreamProducer {

    private final RedisStreamPublisher redisStreamPublisher;
    private final RedisStreamProperties redisStreamProperties;

    /**
     * 방 입장 이벤트를 Redis Stream으로 브로드캐스트합니다.
     *
     * @param event 방 입장 이벤트
     * @throws RuntimeException Stream 발행 실패 시
     */
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
