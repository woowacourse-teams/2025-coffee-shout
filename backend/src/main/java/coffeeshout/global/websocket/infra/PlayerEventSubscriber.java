package coffeeshout.global.websocket.infra;

import coffeeshout.global.websocket.event.player.PlayerBaseEvent;
import coffeeshout.global.websocket.event.player.PlayerDisconnectedEvent;
import coffeeshout.global.websocket.event.player.PlayerEventType;
import coffeeshout.global.websocket.event.player.PlayerReconnectedEvent;
import coffeeshout.global.websocket.infra.handler.PlayerEventHandler;
import coffeeshout.global.websocket.infra.handler.PlayerEventHandlerFactory;
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
public class PlayerEventSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final RedisMessageListenerContainer redisMessageListenerContainer;
    private final ChannelTopic playerEventTopic;
    private final PlayerEventHandlerFactory handlerFactory;
    private final ObservationRegistry observationRegistry;

    @PostConstruct
    public void subscribe() {
        redisMessageListenerContainer.addMessageListener(this, playerEventTopic);
        log.info("플레이어 이벤트 구독 시작: topic={}", playerEventTopic.getTopic());
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String body = null;
        PlayerEventType eventType = null;

        try {
            body = new String(message.getBody());
            eventType = extractEventType(body);

            if (!handlerFactory.canHandle(eventType)) {
                log.warn("처리할 수 없는 플레이어 이벤트 타입: {}", eventType);
                return;
            }

            final PlayerBaseEvent event = deserializeEvent(body, eventType);
            final PlayerEventType finalEventType = eventType;
            final String observationName = "redis.event.processing";

            // Observation을 사용한 트레이싱 및 메트릭 수집
            Observation.createNotStarted(observationName, observationRegistry)
                    .lowCardinalityKeyValue("channel", playerEventTopic.getTopic())
                    .lowCardinalityKeyValue("event.type", finalEventType.name())
                    .highCardinalityKeyValue("event.id", event.getEventId())
                    .observe(() -> {
                        final PlayerEventHandler<PlayerBaseEvent> handler = handlerFactory.getHandler(finalEventType);
                        handler.handle(event);

                        // 이벤트 발행부터 처리까지의 전체 지연 시간 기록
                        final Duration eventAge = Duration.between(event.getTimestamp(), LocalDateTime.now());
                        log.debug("Redis 이벤트 처리 완료: channel={}, eventType={}, eventAge={}ms",
                                playerEventTopic.getTopic(), finalEventType.name(), eventAge.toMillis());
                    });

        } catch (Exception e) {
            log.error("플레이어 이벤트 처리 실패: message={}, eventType={}", body, eventType, e);
            if (eventType != null) {
                final PlayerEventType finalEventTypeForError = eventType;
                // 에러도 Observation으로 기록하여 트레이싱에 남김
                Observation.createNotStarted("redis.event.error", observationRegistry)
                        .lowCardinalityKeyValue("channel", playerEventTopic.getTopic())
                        .lowCardinalityKeyValue("event.type", finalEventTypeForError.name())
                        .lowCardinalityKeyValue("error.type", e.getClass().getSimpleName())
                        .error(e)
                        .observe(() -> {});
            }
        }
    }

    private PlayerEventType extractEventType(String body) throws Exception {
        final JsonNode jsonNode = objectMapper.readTree(body);
        final String eventTypeStr = jsonNode.get("eventType").asText();
        return PlayerEventType.valueOf(eventTypeStr);
    }

    private PlayerBaseEvent deserializeEvent(String body, PlayerEventType eventType) throws Exception {
        return switch (eventType) {
            case PLAYER_DISCONNECTED -> objectMapper.readValue(body, PlayerDisconnectedEvent.class);
            case PLAYER_RECONNECTED -> objectMapper.readValue(body, PlayerReconnectedEvent.class);
        };
    }
}
