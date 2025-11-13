package coffeeshout.global.infra.messaging;

import coffeeshout.global.config.properties.RedisStreamProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisStreamCommands.XAddOptions;
import org.springframework.data.redis.connection.stream.Record;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis Stream 공통 발행 컴포넌트
 * <p>
 * Redis Stream으로 이벤트를 발행하는 공통 로직을 제공합니다.
 * 이벤트 직렬화, Record 생성, Stream 추가 등의 반복적인 작업을 캡슐화하여
 * 코드 중복을 제거하고 일관성을 보장합니다.
 * </p>
 *
 * <p><b>사용 방법:</b></p>
 * <pre>
 * // 특정 도메인 Producer에서 합성(composition)을 통해 사용
 * public class RoomEnterStreamProducer {
 *     private final RedisStreamPublisher redisStreamPublisher;
 *
 *     public void broadcastEnterRoom(RoomJoinEvent event) {
 *         redisStreamPublisher.publish(
 *             redisStreamProperties.roomJoinKey(),
 *             event,
 *             "방 입장 이벤트"
 *         );
 *     }
 * }
 * </pre>
 *
 * <p><b>특징:</b></p>
 * <ul>
 *   <li>이벤트 타입에 무관한 범용 발행 메커니즘</li>
 *   <li>JSON 직렬화 자동 처리</li>
 *   <li>Stream maxlen 설정을 통한 메모리 관리</li>
 *   <li>상세한 로깅 및 예외 처리</li>
 * </ul>
 *
 * @see RoomEnterStreamProducer
 * @see CardSelectStreamProducer
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisStreamPublisher {

    private final StringRedisTemplate stringRedisTemplate;
    private final RedisStreamProperties redisStreamProperties;
    private final ObjectMapper objectMapper;

    /**
     * Redis Stream으로 이벤트를 발행합니다.
     * <p>
     * 이벤트를 JSON으로 직렬화하고 지정된 Stream 키에 추가합니다.
     * maxlen 설정을 통해 Stream 크기를 제한하여 메모리를 관리합니다.
     * </p>
     *
     * @param streamKey   발행할 Redis Stream 키
     * @param event       발행할 이벤트 객체 (직렬화 가능해야 함)
     * @param description 로그에 사용될 이벤트 설명 (예: "방 입장 이벤트")
     * @throws RuntimeException 직렬화 실패 또는 Stream 발행 실패 시
     */
    public void publish(String streamKey, Object event, String description) {
        log.info("{} 발송 시작: streamKey={}, event={}", description, streamKey, event);

        try {
            // 1. 이벤트를 JSON으로 직렬화
            final String eventJson = objectMapper.writeValueAsString(event);

            // 2. Redis Stream Record 생성
            final Record<String, Object> record = StreamRecords.newRecord()
                    .in(streamKey)
                    .ofObject(eventJson);

            // 3. Stream에 추가 (maxlen 설정으로 메모리 제한)
            final var recordId = stringRedisTemplate.opsForStream().add(
                    record,
                    XAddOptions.maxlen(redisStreamProperties.maxLength())
                            .approximateTrimming(true)
            );

            log.info("{} 발송 성공: recordId={}, streamKey={}", description, recordId, streamKey);

        } catch (JsonProcessingException e) {
            log.error("{} 직렬화 실패: streamKey={}, event={}", description, streamKey, event, e);
            throw new RuntimeException(description + " 직렬화 실패: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("{} 발송 실패: streamKey={}, event={}", description, streamKey, event, e);
            throw new RuntimeException(description + " 발송 실패: " + e.getMessage(), e);
        }
    }
}
