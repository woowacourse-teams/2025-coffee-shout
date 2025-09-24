package coffeeshout.global.interceptor;

import coffeeshout.global.metric.WebSocketMetricService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketInboundMetricInterceptor implements ChannelInterceptor {

    private final WebSocketMetricService webSocketMetricService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        final StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        final Object commandObj = accessor.getCommand();

        // STOMP 명령이 아닌 내부 메시지는 무시
        if (!(commandObj instanceof StompCommand)) {
            return message;
        }

        try {
            webSocketMetricService.incrementInboundMessage();
        } catch (Exception e) {
            log.error("WebSocket 인바운드 메트릭 수집 중 에러", e);
        }

        return message;
    }
}