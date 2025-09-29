package coffeeshout.minigame.infra.messaging;

import coffeeshout.global.config.properties.RedisStreamProperties;
import coffeeshout.minigame.domain.cardgame.event.SelectCardCommandEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisStreamCommands.XAddOptions;
import org.springframework.data.redis.connection.stream.Record;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CardSelectStreamProducer {

    private final StringRedisTemplate stringRedisTemplate;
    private final RedisStreamProperties redisStreamProperties;
    private final ObjectMapper objectMapper;

    public void broadcastCardSelect(SelectCardCommandEvent event) {
        log.info("Broadcasting card select event: eventId={}, playerName={}, selectedCard={}",
                event.eventId(), event.playerName(), event.cardIndex());

        try {
            String value = objectMapper.writeValueAsString(event);

            Record<String, Object> objectRecord = StreamRecords.newRecord()
                    .in(redisStreamProperties.cardGameSelectKey())
                    .ofObject(value);

            var recordId = stringRedisTemplate.opsForStream().add(
                    objectRecord,
                    XAddOptions.maxlen(redisStreamProperties.maxLength()).approximateTrimming(true)
            );

            log.info("Card select broadcast sent: recordId={}", recordId);
        } catch (JsonProcessingException e) {
            log.error("직렬화 중 예외가 발생했습니다. eventId = {}", event.eventId(), e);
            throw new RuntimeException("SelectCardCommandEvent 직렬화 실패", e);
        } catch (Exception e) {
            log.error("Failed to broadcast card select event", e);
            throw new RuntimeException("Failed to broadcast card select event", e);
        }
    }
}
