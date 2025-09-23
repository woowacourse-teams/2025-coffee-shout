package coffeeshout.global.websocket;

import coffeeshout.global.trace.SpanRepository;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class LoggingSimpMessagingTemplate {

    private final SimpMessagingTemplate messagingTemplate;

    @WithSpan("websocket.message.send")
    public void convertAndSend(String destination, Object payload) {
        final Message<?> message = generateTracableMessage(payload);
        messagingTemplate.send(destination, message);
    }

    private Message<?> generateTracableMessage(Object payload) {
        final UUID uuid = UUID.randomUUID();
        final SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.create();
        accessor.setHeader("otelSpan", uuid);
        accessor.setLeaveMutable(true);
        SpanRepository.add(uuid, Span.current());
        return MessageBuilder.createMessage(payload, accessor.getMessageHeaders());
    }
}
