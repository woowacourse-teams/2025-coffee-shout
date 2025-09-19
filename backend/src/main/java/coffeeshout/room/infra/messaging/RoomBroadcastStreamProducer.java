package coffeeshout.room.infra.messaging;

import coffeeshout.global.messaging.RedisStreamBroadcastService;
import coffeeshout.room.domain.event.RoomJoinEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomBroadcastStreamProducer {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final RoomJoinEventConverter roomJoinEventConverter;

    @Value("${spring.application.name:app}")
    private String applicationName;

    @Value("${server.port:8080}")
    private String serverPort;

    public void broadcastEnterRoom(RoomJoinEvent event) {
        log.info("Broadcasting enter room event: joinCode={}, playerName={}", event.joinCode(),
                event.guestName());

        try {
            // Converter를 사용해서 Event를 플랫 Map으로 변환
            Map<String, String> flatMap = roomJoinEventConverter.toFlatMap(event);

            RecordId recordId = stringRedisTemplate.opsForStream().add(
                    RedisStreamBroadcastService.BROADCAST_STREAM,
                    flatMap
            );

            log.info("Enter room broadcast sent: recordId={}", recordId.getValue());
        } catch (Exception e) {
            log.error("Failed to broadcast enter room event", e);
            throw new RuntimeException("Failed to broadcast enter room event", e);
        }
    }


    private String getInstanceId() {
        return applicationName + "-" + serverPort;
    }
}
