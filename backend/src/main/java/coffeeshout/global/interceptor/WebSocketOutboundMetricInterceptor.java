package coffeeshout.global.interceptor;

import coffeeshout.global.metric.WebSocketMetricService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.support.ExecutorChannelInterceptor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketOutboundMetricInterceptor implements ExecutorChannelInterceptor {

    private final WebSocketMetricService webSocketMetricService;

    @Override
    public Message<?> beforeHandle(Message<?> message, MessageChannel channel, MessageHandler handler) {
        return message;
    }

    @Override
    public void afterMessageHandled(
            Message<?> message,
            MessageChannel channel,
            MessageHandler handler,
            Exception exception
    ) {
        final var type = SimpMessageHeaderAccessor.getMessageType(message.getHeaders());
        if (SimpMessageType.HEARTBEAT.equals(type)) {
            return;
        }

        try {
            webSocketMetricService.incrementOutboundMessage();
        } catch (Exception e) {
            log.error("WebSocket 아웃바운드 메트릭 수집 중 에러", e);
        }
    }
}
