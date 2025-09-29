package coffeeshout.room.infra.messaging;

import coffeeshout.global.config.properties.RedisStreamProperties;
import coffeeshout.room.domain.event.RoomJoinEvent;
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
public class RoomEnterStreamProducer {

    private final StringRedisTemplate stringRedisTemplate;
    private final RedisStreamProperties redisStreamProperties;
    private final ObjectMapper objectMapper;

    public void broadcastEnterRoom(RoomJoinEvent event) {
        log.info("Broadcasting enter room event: joinCode={}, playerName={}",
                event.joinCode(), event.guestName());

        try {
            String eventJson = objectMapper.writeValueAsString(event);
            Record<String, String> objectRecord = StreamRecords.newRecord()
                    .in(redisStreamProperties.roomJoinKey())
                    .ofObject(eventJson);

            var recordId = stringRedisTemplate.opsForStream().add(
                    objectRecord,
                    XAddOptions.maxlen(redisStreamProperties.maxLength()).approximateTrimming(true)
            );

            log.info("Enter room broadcast sent: recordId={}", recordId);
        } catch (JsonProcessingException e){
            log.error("직렬화 중 예외가 발생했습니다. event = {}, ", event, e);
            throw new RuntimeException("RoomJoinEvent 직렬화 실패", e);
        } catch (Exception e) {
            log.error("Failed to broadcast enter room event. event = {}", event, e);
            throw new RuntimeException("Failed to broadcast enter room event", e);
        }
    }
}
