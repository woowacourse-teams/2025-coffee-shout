package coffeeshout.room.infra;

import coffeeshout.room.domain.event.MiniGameSelectEvent;
import coffeeshout.room.domain.event.PlayerListUpdateEvent;
import coffeeshout.room.domain.event.PlayerReadyEvent;
import coffeeshout.room.domain.event.RoomBaseEvent;
import coffeeshout.room.domain.event.RoomCreateEvent;
import coffeeshout.room.domain.event.RoomEventType;
import coffeeshout.room.domain.event.RoomJoinEvent;
import coffeeshout.room.domain.event.RouletteShowEvent;
import coffeeshout.room.domain.event.RouletteSpinEvent;
import coffeeshout.room.infra.handler.RoomEventHandler;
import coffeeshout.room.infra.handler.RoomEventHandlerFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomEventSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final RedisMessageListenerContainer redisMessageListenerContainer;
    private final ChannelTopic roomEventTopic;
    private final RoomEventHandlerFactory handlerFactory;
    private final ObservationRegistry observationRegistry;

    @PostConstruct
    public void subscribe() {
        redisMessageListenerContainer.addMessageListener(this, roomEventTopic);
        log.info("방 이벤트 구독 시작: topic={}", roomEventTopic.getTopic());
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String body = null;
        RoomEventType eventType = null;

        try {
            body = new String(message.getBody());
            eventType = extractEventType(body);

            if (!handlerFactory.canHandle(eventType)) {
                log.warn("처리할 수 없는 이벤트 타입: {}", eventType);
                return;
            }

            final RoomBaseEvent event = deserializeEvent(body, eventType);
            final RoomEventType finalEventType = eventType;
            final String observationName = "redis.event.processing";

            // Observation을 사용한 트레이싱 및 메트릭 수집
            Observation.createNotStarted(observationName, observationRegistry)
                    .lowCardinalityKeyValue("channel", roomEventTopic.getTopic())
                    .lowCardinalityKeyValue("event.type", finalEventType.name())
                    .highCardinalityKeyValue("event.id", event.getEventId())
                    .observe(() -> {
                        final RoomEventHandler<RoomBaseEvent> handler = handlerFactory.getHandler(finalEventType);
                        handler.handle(event);

                        // 이벤트 발행부터 처리까지의 전체 지연 시간 기록
                        final Duration eventAge = Duration.between(event.getTimestamp(), LocalDateTime.now());
                        log.debug("Redis 이벤트 처리 완료: channel={}, eventType={}, eventAge={}ms",
                                roomEventTopic.getTopic(), finalEventType.name(), eventAge.toMillis());
                    });

        } catch (Exception e) {
            log.error("이벤트 처리 실패: message={}, eventType={}", body, eventType, e);
            if (eventType != null) {
                final RoomEventType finalEventTypeForError = eventType;
                // 에러도 Observation으로 기록하여 트레이싱에 남김
                Observation.createNotStarted("redis.event.error", observationRegistry)
                        .lowCardinalityKeyValue("channel", roomEventTopic.getTopic())
                        .lowCardinalityKeyValue("event.type", finalEventTypeForError.name())
                        .lowCardinalityKeyValue("error.type", e.getClass().getSimpleName())
                        .error(e)
                        .observe(() -> {});
            }
        }
    }

    private RoomEventType extractEventType(String body) throws Exception {
        final JsonNode jsonNode = objectMapper.readTree(body);
        final String eventTypeStr = jsonNode.get("eventType").asText();
        return RoomEventType.valueOf(eventTypeStr);
    }

    private RoomBaseEvent deserializeEvent(String body, RoomEventType eventType) throws Exception {
        return switch (eventType) {
            case ROOM_CREATE -> objectMapper.readValue(body, RoomCreateEvent.class);
            case ROOM_JOIN -> objectMapper.readValue(body, RoomJoinEvent.class);
            case PLAYER_LIST_UPDATE -> objectMapper.readValue(body, PlayerListUpdateEvent.class);
            case PLAYER_READY -> objectMapper.readValue(body, PlayerReadyEvent.class);
            case MINI_GAME_SELECT -> objectMapper.readValue(body, MiniGameSelectEvent.class);
            case ROULETTE_SHOW -> objectMapper.readValue(body, RouletteShowEvent.class);
            case ROULETTE_SPIN -> objectMapper.readValue(body, RouletteSpinEvent.class);
        };
    }
}
