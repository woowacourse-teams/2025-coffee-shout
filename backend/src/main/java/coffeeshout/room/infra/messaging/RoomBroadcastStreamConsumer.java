package coffeeshout.room.infra.messaging;

import coffeeshout.global.config.properties.RedisStreamProperties;
import coffeeshout.room.application.RoomService;
import coffeeshout.room.domain.event.RoomEventType;
import coffeeshout.room.domain.event.RoomJoinEvent;
import jakarta.annotation.PostConstruct;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomBroadcastStreamConsumer implements StreamListener<String, MapRecord<String, String, String>> {

    public static final String BROADCAST = "room:broadcast";

    private final RoomService roomService;
    private final StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer;
    private final RoomEventWaitManager roomEventWaitManager;
    private final RoomJoinEventConverter roomJoinEventConverter;
    private final RedisStreamProperties redisStreamProperties;

    /**
     * StreamMessageListenerContainer에 리스너 등록
     */
    @PostConstruct
    public void registerListener() {
        // 단독 소비자 패턴으로 스트림 리스너 등록
        log.info("roomKey : {}", redisStreamProperties.roomKey());

        listenerContainer.receive(
                StreamOffset.fromStart(BROADCAST),
                this
        );

        log.info("Registered broadcast stream listener for: {}", redisStreamProperties.roomKey());
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
}
