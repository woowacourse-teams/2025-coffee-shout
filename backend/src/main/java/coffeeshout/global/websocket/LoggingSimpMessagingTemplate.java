package coffeeshout.global.websocket;

import coffeeshout.global.config.InstanceConfig;
import coffeeshout.global.redis.RedisMessagePublisher;
import coffeeshout.global.redis.event.websocket.WebSocketBroadcastEvent;
import coffeeshout.global.ui.WebSocketResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoggingSimpMessagingTemplate {

    private final SimpMessagingTemplate messagingTemplate;
    private final RedisMessagePublisher messagePublisher;
    private final InstanceConfig instanceConfig;

    public void convertAndSend(String destination, Object payload) {
        WebSocketResponse response = (WebSocketResponse) payload;
        long startNanos = System.nanoTime();
        
        // 로컬 웹소켓 전송
        messagingTemplate.convertAndSend(destination, payload);
        
        long durationNanos = System.nanoTime() - startNanos;
        double durationMs = durationNanos / 1_000_000.0;

        log.info("WebSocket 메시지 전송 - destination: {}, success: {}, duration: {}ms",
                destination,
                response.success(),
                durationMs
        );

        // 다른 인스턴스들에게도 웹소켓 메시지 전송 (Redis pub/sub)
        publishWebSocketMessage(destination, payload);
    }

    private void publishWebSocketMessage(String destination, Object payload) {
        try {
            messagePublisher.publishWebSocketMessage(new WebSocketBroadcastEvent(
                destination,
                payload,
                instanceConfig.getInstanceId()
            ));
        } catch (Exception e) {
            log.error("웹소켓 메시지 Redis 발행 실패: destination={}, error={}", 
                     destination, e.getMessage(), e);
        }
    }
}
