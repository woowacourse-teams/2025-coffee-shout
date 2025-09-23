package coffeeshout.global.websocket;

import coffeeshout.global.trace.SpanRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    @WithSpan("websocket.message.send")
    public void convertAndSend(String destination, Object payload) {
        final Message<?> message = generateTraceableMessage(payload);
        messagingTemplate.send(destination, message);
    }

    private Message<?> generateTraceableMessage(Object payload) {
        final UUID uuid = UUID.randomUUID();
        final SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.create();
        accessor.setHeader("otelSpan", uuid);
        accessor.setLeaveMutable(true);

        SpanRepository.add(uuid, Span.current());

        try {
            byte[] serialized = objectMapper.writeValueAsBytes(payload);
            return MessageBuilder.createMessage(serialized, accessor.getMessageHeaders());
        } catch (JsonProcessingException exception) {
            SpanRepository.endSpan(uuid, exception);
            throw new RuntimeException("메시지 직렬화 실패", exception);
        }
    }
}
