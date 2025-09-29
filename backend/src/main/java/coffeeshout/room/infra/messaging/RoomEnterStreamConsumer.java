package coffeeshout.room.infra.messaging;

import coffeeshout.global.config.properties.RedisStreamProperties;
import coffeeshout.global.message.RedisStreamStartStrategy;
import coffeeshout.room.application.RoomService;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.event.RoomJoinEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomEnterStreamConsumer implements StreamListener<String, ObjectRecord<String, String>> {

    private final RoomService roomService;
    private final RoomEventWaitManager roomEventWaitManager;
    private final StreamMessageListenerContainer<String, ObjectRecord<String, String>> roomEnterStreamContainer;
    private final RedisStreamStartStrategy redisStreamStartStrategy;
    private final RedisStreamProperties redisStreamProperties;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void registerListener() {
        roomEnterStreamContainer.receive(
                redisStreamStartStrategy.getStreamOffset(redisStreamProperties.roomJoinKey()),
                this
        );

        log.info("Registered room enter stream listener for: {}", redisStreamProperties.roomJoinKey());
    }

    @Override
    public void onMessage(ObjectRecord<String, String> message) {
        RoomJoinEvent event = parseEvent(message);
        log.info("Received room enter message: id={}, event={}",
                message.getId(), event);
        try {
            Room room = roomService.enterRoom(event.joinCode(), event.guestName(), event.selectedMenuRequest());
            roomEventWaitManager.notifySuccess(event.eventId(), room);
        } catch (Exception e) {
            log.error("Failed to process room enter message: {}", message, e);
            roomEventWaitManager.notifyFailure(event.eventId(), e);
        }
    }

    private RoomJoinEvent parseEvent(ObjectRecord<String, String> message) {
        try {
            String jsonValue = message.getValue();
            String value = objectMapper.readValue(jsonValue, String.class);
            return objectMapper.readValue(value, RoomJoinEvent.class);
        } catch (JsonProcessingException e) {
            log.error("RoomJoinEvent 파싱 실패: message={}", message, e);
            throw new IllegalArgumentException(e);
        }
    }
}
