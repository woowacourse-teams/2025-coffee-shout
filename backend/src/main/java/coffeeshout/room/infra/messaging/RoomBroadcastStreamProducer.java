package coffeeshout.room.infra.messaging;

import coffeeshout.global.config.properties.RedisStreamProperties;
import coffeeshout.room.domain.event.RoomJoinEvent;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisStreamCommands.XAddOptions;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomBroadcastStreamProducer {

    private final StringRedisTemplate stringRedisTemplate;
    private final RoomJoinEventConverter roomJoinEventConverter;
    private final RedisStreamProperties redisStreamProperties;

    public void broadcastEnterRoom(RoomJoinEvent event) {
        log.info("Broadcasting enter room event: joinCode={}, playerName={}", event.joinCode(),
                event.guestName());

        try {
            // Converter를 사용해서 Event를 플랫 Map으로 변환
            Map<String, String> flatMap = roomJoinEventConverter.toFlatMap(event);

            RecordId recordId = stringRedisTemplate.opsForStream().add(
                    redisStreamProperties.roomJoinKey(),
                    flatMap,
                    XAddOptions.maxlen(redisStreamProperties.maxLength()).approximateTrimming(true)
            );

            log.info("Enter room broadcast sent: recordId={}", recordId.getValue());
        } catch (Exception e) {
            log.error("Failed to broadcast enter room event", e);
            throw new RuntimeException("Failed to broadcast enter room event", e);
        }
    }
}
