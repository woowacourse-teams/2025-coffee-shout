package coffeeshout.room.infra.messaging;

import coffeeshout.global.messaging.RedisStreamBroadcastService;
import coffeeshout.room.application.RoomService;
import coffeeshout.room.domain.event.RoomEventType;
import coffeeshout.room.domain.event.RoomJoinEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomBroadcastStreamConsumer implements StreamListener<String, MapRecord<String, String, String>> {

    private final ObjectMapper objectMapper;
    private final RoomService roomService;
    private final RedisStreamBroadcastService broadcastService;
    private final StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer;
    private final StringRedisTemplate stringRedisTemplate;
    private final RoomEventWaitManager roomEventWaitManager;
    private final RoomJoinEventConverter roomJoinEventConverter;

    @Value("${spring.application.name:app}")
    private String applicationName;

    @Value("${server.port:8080}")
    private String serverPort;

    /**
     * StreamMessageListenerContainer에 리스너 등록
     */
    @PostConstruct
    public void registerListener() {
        // 단독 소비자 패턴으로 스트림 리스너 등록
        listenerContainer.receive(
                StreamOffset.fromStart(RedisStreamBroadcastService.BROADCAST_STREAM),
                this
        );

        log.info("Registered broadcast stream listener for: {}", RedisStreamBroadcastService.BROADCAST_STREAM);
    }

    /**
     * StreamListener 인터페이스 구현 - 메시지가 도착하면 자동 호출
     */
    @Override
    public void onMessage(MapRecord<String, String, String> mapRecord) {
        try {
            log.info("Received broadcast message: id={}, value={}",
                    mapRecord.getId(), mapRecord.getValue());
            processBroadcastRecordV2(mapRecord);
        } catch (Exception e) {
            log.error("Failed to process broadcast message", e);
        }
    }

    private void processBroadcastRecordV2(MapRecord<String, String, String> mapRecord) {
        Map<String, String> messageValue = mapRecord.getValue();

        // 기존 다른 메시지 타입들 처리 (UPDATE_ROOM_STATE, SYNC_ROOM_DATA 등)
        if (!messageValue.containsKey("eventType")) {
            log.warn("Received message without eventType: id={}, value={}", mapRecord.getId(), messageValue);
            throw new IllegalArgumentException("Missing eventType in message");
        }

        String eventId = messageValue.get("eventId");
        try {
            RoomEventType eventType = RoomEventType.valueOf(messageValue.get("eventType"));

            Object result = switch (eventType) {
                case ROOM_JOIN -> handleJoinRoomBroadcast(mapRecord);
                default -> throw new IllegalArgumentException("Unknown broadcast eventType: " + eventType);
            };

            roomEventWaitManager.notifySuccess(eventId, result);

        } catch (final Exception e) {
            roomEventWaitManager.notifyFailure(eventId, e);
        }
    }


    private Object handleJoinRoomBroadcast(MapRecord<String, String, String> mapRecord) {
        // Converter를 사용해서 플랫 Map에서 RoomJoinEvent 재구성
        RoomJoinEvent roomJoinEvent = roomJoinEventConverter.fromFlatMap(mapRecord.getValue());

        return roomService.enterRoom(roomJoinEvent.joinCode(), roomJoinEvent.guestName(),
                roomJoinEvent.selectedMenuRequest());
    }

    private String getInstanceId() {
        return applicationName + "-" + serverPort;
    }
}
