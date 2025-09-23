package coffeeshout.minigame.infra.messaging;

import coffeeshout.global.config.properties.RedisStreamProperties;
import coffeeshout.minigame.domain.cardgame.event.SelectCardCommandEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisStreamCommands.XAddOptions;
import org.springframework.data.redis.connection.stream.Record;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CardSelectStreamProducer {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisStreamProperties redisStreamProperties;
    private final ObjectMapper objectMapper;

    public void broadcastCardSelect(SelectCardCommandEvent event) {
        log.info("Broadcasting card select event: gameId={}, playerKey={}, selectedCard={}",
                event.eventId(), event.playerName(), event.cardIndex());

        try {
            String eventJson = objectMapper.writeValueAsString(event);
            log.debug("eventJson={}", eventJson);

            Record<String, String> record = StreamRecords.newRecord()
                    .in(redisStreamProperties.cardGameSelectKey())
                    .ofObject(eventJson);

            var recordId = redisTemplate.opsForStream().add(
                    record,
                    XAddOptions.maxlen(redisStreamProperties.maxLength()).approximateTrimming(true)
            );

            log.info("Card select broadcast sent: recordId={}", recordId.getValue());
        } catch (Exception e) {
            log.error("Failed to broadcast card select event", e);
            throw new RuntimeException("Failed to broadcast card select event", e);
        }
    }
}
