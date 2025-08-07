package coffeeshout.global.websocket;

import coffeeshout.global.ui.WebSocketResponse;
import io.micrometer.cloudwatch2.CloudWatchMeterRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LoggingSimpMessagingTemplate {

    private final SimpMessagingTemplate messagingTemplate;
    private final Timer messageSendTimer;

    public LoggingSimpMessagingTemplate(
            SimpMessagingTemplate messagingTemplate,
            CloudWatchMeterRegistry meterRegistry
    ) {
        this.messagingTemplate = messagingTemplate;
        this.messageSendTimer = Timer.builder("websocket.message.send.time")
                .description("웹소켓 메시지 전송 시간")
                .register(meterRegistry);
    }

    public void convertAndSend(String destination, Object payload) {
        WebSocketResponse response = (WebSocketResponse) payload;

        Timer.Sample sample = Timer.start();
        messagingTemplate.convertAndSend(destination, payload);
        long durationNanos = sample.stop(messageSendTimer);
        double durationMs = durationNanos / 1_000_000.0;

        log.info("WebSocket 메시지 전송 - destination: {}, success: {}, duration: {}ms",
                destination,
                response.success(),
                durationMs
        );
    }
}
