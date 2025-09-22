package coffeeshout.global.websocket;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class LoggingSimpMessagingTemplate {

    private final SimpMessagingTemplate messagingTemplate;
    private final TextMapPropagator propagator;

    @WithSpan("websocket.message.send")
    public void convertAndSend(String destination, Object payload) {
        // 현재 Context 가져오기
        Context currentContext = Context.current();

        // traceparent, tracestate를 Map에 주입

        Map<String, String> contextHeaders = new HashMap<>();
        propagator.inject(currentContext, contextHeaders, (carrier, key, value) -> carrier.put(key, value));
        // String Map → Object Map 변환 (STOMP 헤더는 Object 타입 허용)
        Map<String, Object> headers = new HashMap<>(contextHeaders);

        // 헤더를 포함해 전송
        messagingTemplate.convertAndSend(destination, payload, headers);
    }}
