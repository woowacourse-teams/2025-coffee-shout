package coffeeshout.global.websocket;

import coffeeshout.global.ui.WebSocketResponse;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoggingSimpMessagingTemplate {

    private final SimpMessagingTemplate messagingTemplate;

    @WithSpan("websocket.message.send")
    public void convertAndSend(String destination, Object payload) {
        WebSocketResponse response = (WebSocketResponse) payload;
        long startNanos = System.nanoTime();
        messagingTemplate.convertAndSend(destination, payload);
        long durationNanos = System.nanoTime() - startNanos;
        double durationMs = durationNanos / 1_000_000.0;

        log.info("WebSocket 메시지 전송 - destination: {}, success: {}, duration: {}ms",
                destination,
                response.success(),
                durationMs
        );
    }
}
