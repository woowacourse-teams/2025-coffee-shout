package coffeeshout.room.infra.messaging;

import coffeeshout.global.config.redis.EventSubscriber;
import coffeeshout.global.config.redis.EventTopicRegistry;
import coffeeshout.global.trace.Traceable;
import coffeeshout.global.trace.TracerProvider;
import coffeeshout.room.application.port.RoomEventHandler;
import coffeeshout.room.domain.event.RoomBaseEvent;
import coffeeshout.room.domain.event.RoomEventType;
import coffeeshout.room.infra.messaging.handler.RoomEventHandlerFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomEventSubscriber implements EventSubscriber {

    private final ObjectMapper objectMapper;
    private final RoomEventHandlerFactory handlerFactory;
    private final TracerProvider tracerProvider;

    @Override
    public EventTopicRegistry getTopicRegistry() {
        return EventTopicRegistry.ROOM;
    }

    @Override
    public void onMessage(@NonNull Message message, byte[] pattern) {
        try {
            final String body = new String(message.getBody());
            final RoomEventType eventType = extractEventType(body);

            if (!handlerFactory.canHandle(eventType)) {
                log.warn("처리할 수 없는 이벤트 타입: {}", eventType);
                return;
            }

            final RoomBaseEvent event = objectMapper.readValue(body, eventType.getEventClass());
            final RoomEventHandler<RoomBaseEvent> handler = handlerFactory.getHandler(eventType);
            if (event instanceof Traceable traceable) {
                tracerProvider.executeWithTraceContext(
                        traceable.getTraceInfo(),
                        () -> handler.handle(event),
                        event.eventType().name()
                );
                return;
            }
            handler.handle(event);

        } catch (Exception e) {
            log.error("이벤트 처리 실패: message={}", new String(message.getBody()), e);
        }
    }

    private RoomEventType extractEventType(String body) throws Exception {
        final JsonNode jsonNode = objectMapper.readTree(body);
        final String eventTypeStr = jsonNode.get("eventType").asText();
        return RoomEventType.valueOf(eventTypeStr);
    }
}
