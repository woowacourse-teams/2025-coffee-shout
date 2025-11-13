package coffeeshout.room.infra.messaging;

import coffeeshout.global.config.properties.RedisStreamProperties;
import coffeeshout.global.config.redis.EventTopicRegistry;
import coffeeshout.global.config.redis.TopicManager;
import coffeeshout.room.domain.event.RoomEventPublisher;
import coffeeshout.room.domain.event.RoomBaseEvent;
import coffeeshout.room.domain.event.RoomJoinEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.Record;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import static org.springframework.data.redis.connection.RedisStreamCommands.XAddOptions;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisRoomEventPublisher implements RoomEventPublisher {

    private final RedisTemplate<String, String> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final TopicManager topicManager;
    private final RedisStreamProperties redisStreamProperties;
    private final ObjectMapper objectMapper;

    @Override
    public <T extends RoomBaseEvent> void publish(T event) {
        try {
            // 이벤트 타입에 따라 발행 메커니즘 선택
            // TODO: 이벤트 타입이 증가하면 전략 패턴으로 리팩토링 고려
            if (event instanceof RoomJoinEvent roomJoinEvent) {
                publishToStream(roomJoinEvent);
            } else {
                publishToPubSub(event);
            }

            log.info("이벤트 발행 완료: eventType={}, eventId={}",
                    event.eventType(), event.eventId());

        } catch (Exception e) {
            log.error("이벤트 발행 실패: eventType={}, eventId={}",
                    event.eventType(), event.eventId(), e);
            throw new RuntimeException("이벤트 발행 실패", e);
        }
    }

    /**
     * Redis Pub/Sub으로 이벤트 발행
     * <p>
     * <b>특징:</b>
     * <ul>
     *   <li>즉시 브로드캐스트 - 모든 구독자에게 전파</li>
     *   <li>구독자가 없어도 정상 동작 (fire-and-forget)</li>
     *   <li>빠른 이벤트 전파 (메모리 기반)</li>
     * </ul>
     * </p>
     *
     * @param event 발행할 이벤트
     */
    private void publishToPubSub(RoomBaseEvent event) {
        String topic = topicManager.getTopic(EventTopicRegistry.ROOM).getTopic();
        redisTemplate.convertAndSend(topic, event);

        log.debug("Redis Pub/Sub로 이벤트 발행: topic={}, eventType={}",
                topic, event.eventType());
    }

    private void publishToStream(RoomJoinEvent event) {
        log.debug("방 입장 이벤트 Stream 발행 시작: eventId={}, joinCode={}, guestName={}",
                event.eventId(), event.joinCode(), event.guestName());

        try {
            String eventJson = objectMapper.writeValueAsString(event);
            Record<String, String> record = StreamRecords.newRecord()
                    .in(redisStreamProperties.roomJoinKey())
                    .ofObject(eventJson);

            RecordId recordId = stringRedisTemplate.opsForStream().add(
                    record,
                    XAddOptions.maxlen(redisStreamProperties.maxLength()).approximateTrimming(true)
            );

            log.debug("방 입장 이벤트 Stream 발행 성공: eventId={}, recordId={}, streamKey={}",
                    event.eventId(), recordId, redisStreamProperties.roomJoinKey());

        } catch (JsonProcessingException e) {
            log.error("이벤트 직렬화 실패: eventId={}, joinCode={}, guestName={}",
                    event.eventId(), event.joinCode(), event.guestName(), e);
            throw new RuntimeException("RoomJoinEvent 직렬화 실패: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("방 입장 이벤트 Stream 발행 실패: eventId={}, joinCode={}, guestName={}",
                    event.eventId(), event.joinCode(), event.guestName(), e);
            throw new RuntimeException("방 입장 이벤트 발행 실패: " + e.getMessage(), e);
        }
    }
}
