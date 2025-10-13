package coffeeshout.minigame.infra;

import coffeeshout.minigame.domain.event.MiniGameBaseEvent;
import coffeeshout.minigame.domain.event.MiniGameEventType;
import coffeeshout.minigame.domain.event.SelectCardCommandEvent;
import coffeeshout.minigame.domain.event.StartMiniGameCommandEvent;
import coffeeshout.minigame.infra.handler.MiniGameEventHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MiniGameEventSubscriber implements MessageListener {

    private final Map<MiniGameEventType, MiniGameEventHandler<MiniGameBaseEvent>> handlers;
    private final ObjectMapper objectMapper;
    private final RedisMessageListenerContainer redisMessageListenerContainer;
    private final ChannelTopic miniGameEventTopic;
    private final ObservationRegistry observationRegistry;

    public MiniGameEventSubscriber(
            List<MiniGameEventHandler<?>> handlers,
            ObjectMapper objectMapper,
            RedisMessageListenerContainer redisMessageListenerContainer,
            ChannelTopic miniGameEventTopic,
            ObservationRegistry observationRegistry
    ) {
        this.handlers = handlers.stream()
                .collect(Collectors.toMap(
                        MiniGameEventHandler::getSupportedEventType,
                        handler -> (MiniGameEventHandler<MiniGameBaseEvent>) handler
                ));
        this.objectMapper = objectMapper;
        this.redisMessageListenerContainer = redisMessageListenerContainer;
        this.miniGameEventTopic = miniGameEventTopic;
        this.observationRegistry = observationRegistry;
    }

    @PostConstruct
    public void subscribe() {
        redisMessageListenerContainer.addMessageListener(this, miniGameEventTopic);
        log.info("미니게임 이벤트 구독 시작: topic={}", miniGameEventTopic.getTopic());
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String body = null;
        MiniGameEventType eventType = null;

        try {
            body = new String(message.getBody());
            eventType = extractEventType(body);

            if (!canHandle(eventType)) {
                log.warn("처리할 수 없는 이벤트 타입: {}", eventType);
                return;
            }

            final MiniGameBaseEvent event = deserializeEvent(body, eventType);
            final MiniGameEventType finalEventType = eventType;
            final String observationName = "redis.event.processing";

            // Observation을 사용한 트레이싱 및 메트릭 수집
            Observation.createNotStarted(observationName, observationRegistry)
                    .lowCardinalityKeyValue("channel", miniGameEventTopic.getTopic())
                    .lowCardinalityKeyValue("event.type", finalEventType.name())
                    .highCardinalityKeyValue("event.id", event.getEventId())
                    .observe(() -> {
                        final MiniGameEventHandler<MiniGameBaseEvent> handler = handlers.get(finalEventType);
                        handler.handle(event);

                        // 이벤트 발행부터 처리까지의 전체 지연 시간 기록
                        final Duration eventAge = Duration.between(event.getCreatedAt(), LocalDateTime.now());
                        log.debug("Redis 이벤트 처리 완료: channel={}, eventType={}, eventAge={}ms",
                                miniGameEventTopic.getTopic(), finalEventType.name(), eventAge.toMillis());
                    });

        } catch (Exception e) {
            log.error("미니게임 이벤트 처리 실패: message={}, eventType={}", body, eventType, e);
            if (eventType != null) {
                final MiniGameEventType finalEventTypeForError = eventType;
                // 에러도 Observation으로 기록하여 트레이싱에 남김
                Observation.createNotStarted("redis.event.error", observationRegistry)
                        .lowCardinalityKeyValue("channel", miniGameEventTopic.getTopic())
                        .lowCardinalityKeyValue("event.type", finalEventTypeForError.name())
                        .lowCardinalityKeyValue("error.type", e.getClass().getSimpleName())
                        .error(e)
                        .observe(() -> {});
            }
        }
    }

    private MiniGameEventType extractEventType(String body) throws Exception {
        final JsonNode jsonNode = objectMapper.readTree(body);
        final String eventTypeStr = jsonNode.get("eventType").asText();
        return MiniGameEventType.valueOf(eventTypeStr);
    }

    private boolean canHandle(MiniGameEventType eventType) {
        return handlers.containsKey(eventType);
    }

    private MiniGameBaseEvent deserializeEvent(String body, MiniGameEventType eventType) throws Exception {
        return switch (eventType) {
            case START_MINIGAME_COMMAND -> objectMapper.readValue(body, StartMiniGameCommandEvent.class);
            case SELECT_CARD_COMMAND -> objectMapper.readValue(body, SelectCardCommandEvent.class);
        };
    }
}
