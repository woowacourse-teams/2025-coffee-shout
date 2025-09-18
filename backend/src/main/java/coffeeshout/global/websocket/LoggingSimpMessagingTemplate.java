package coffeeshout.global.websocket;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LoggingSimpMessagingTemplate {

    private final SimpMessagingTemplate messagingTemplate;
    private final TextMapPropagator propagator;

    public LoggingSimpMessagingTemplate(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
        this.propagator = GlobalOpenTelemetry.getPropagators().getTextMapPropagator();
    }

    @WithSpan("websocket.message.send")
    public void convertAndSend(String destination, Object payload) {
        Context currentContext = Context.current();

        Map<String, String> contextHeaders = new HashMap<>();
        propagator.inject(currentContext, contextHeaders, Map::put);

        // String Map을 Object Map으로 변환 (헤더용)
        Map<String, Object> headers = new HashMap<>();
        contextHeaders.forEach(headers::put);

        messagingTemplate.convertAndSend(destination, payload, headers);
    }
}
