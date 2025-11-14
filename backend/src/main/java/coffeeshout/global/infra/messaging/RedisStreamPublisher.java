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
 * 공통 Stream 발행 - 직렬화/Record 생성/Stream 추가
 * 도메인별 Producer에서 합성(composition)으로 사용
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisStreamPublisher {

    private final StringRedisTemplate stringRedisTemplate;
    private final RedisStreamProperties redisStreamProperties;
    private final ObjectMapper objectMapper;

    public void publish(String streamKey, Object event, String description) {
        log.info("{} 발송 시작: streamKey={}, event={}", description, streamKey, event);

        try {
            final String eventJson = objectMapper.writeValueAsString(event);

            final Record<String, Object> record = StreamRecords.newRecord()
                    .in(streamKey)
                    .ofObject(eventJson);

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
