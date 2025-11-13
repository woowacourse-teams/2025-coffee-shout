package coffeeshout.minigame.infra.messaging;

import coffeeshout.cardgame.domain.event.SelectCardCommandEvent;
import coffeeshout.global.config.redis.EventSubscriber;
import coffeeshout.global.config.redis.EventTopicRegistry;
import coffeeshout.global.trace.TracerProvider;
import coffeeshout.minigame.event.MiniGameBaseEvent;
import coffeeshout.minigame.event.MiniGameEventType;
import coffeeshout.minigame.event.StartMiniGameCommandEvent;
import coffeeshout.minigame.infra.messaging.handler.MiniGameEventHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MiniGameEventSubscriber implements EventSubscriber {

    private final Map<MiniGameEventType, MiniGameEventHandler<MiniGameBaseEvent>> handlers;
    private final ObjectMapper objectMapper;
    private final TracerProvider tracerProvider;

    @SuppressWarnings("unchecked")
    public MiniGameEventSubscriber(
            List<MiniGameEventHandler<?>> handlers,
            ObjectMapper objectMapper,
            TracerProvider tracerProvider
    ) {
        this.handlers = handlers.stream()
                .collect(Collectors.toMap(
                        MiniGameEventHandler::getSupportedEventType,
                        handler -> (MiniGameEventHandler<MiniGameBaseEvent>) handler
                ));
        this.objectMapper = objectMapper;
        this.tracerProvider = tracerProvider;
    }

    @Override
    public EventTopicRegistry getTopicRegistry() {
        return EventTopicRegistry.MINI_GAME;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            final String body = new String(message.getBody());
            final MiniGameEventType eventType = extractEventType(body);

            if (!canHandle(eventType)) {
                log.warn("처리할 수 없는 이벤트 타입: {}", eventType);
                return;
            }

            final MiniGameBaseEvent event = deserializeEvent(body, eventType);
            final MiniGameEventHandler<MiniGameBaseEvent> handler = handlers.get(eventType);
            tracerProvider.executeWithTraceContext(
                    event.traceInfo(),
                    () -> handler.handle(event),
                    event.eventType().name()
            );

        } catch (Exception e) {
            log.error("미니게임 이벤트 처리 실패: message={}", new String(message.getBody()), e);
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
