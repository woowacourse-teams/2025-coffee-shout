package coffeeshout.global.interceptor;

import coffeeshout.global.metric.WebSocketMetricService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ExecutorChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketInboundMetricInterceptor implements ExecutorChannelInterceptor {

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

        final StompCommand command = (StompCommand) commandObj;

        if (command == StompCommand.SEND || command == StompCommand.SUBSCRIBE) {
            String messageId = UUID.randomUUID().toString();
            accessor.setHeader("messageId", messageId);

            try {
                // 큐 대기 시간 측정 시작
                webSocketMetricService.startInboundMessageTimer(messageId);
            } catch (Exception e) {
                log.warn("WebSocket 큐 대기 시간 측정 시작 중 에러", e);
            }
        }

        try {
            webSocketMetricService.incrementInboundMessage();
        } catch (Exception e) {
            log.warn("WebSocket 인바운드 메트릭 수집 중 에러", e);
        }

        return message;
    }

    @Override
    public Message<?> beforeHandle(Message<?> message, MessageChannel channel, MessageHandler handler) {
        final StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        String messageId = (String) accessor.getHeader("messageId");
        if (messageId != null) {
            try {
                webSocketMetricService.stopInboundMessageTimer(messageId);
                webSocketMetricService.startBusinessTimer(messageId);
            } catch (Exception e) {
                log.warn("WebSocket 큐 대기 시간 측정 완료 및 비즈니스 로직 측정 시작 중 에러", e);
            }
        }

        return message;
    }

    @Override
    public void afterMessageHandled(Message<?> message, MessageChannel channel, MessageHandler handler, Exception ex) {
        final StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return;
        }

        String messageId = (String) accessor.getHeader("messageId");
        if (messageId != null) {
            try {
                // 비즈니스 로직 시간 측정 종료
                webSocketMetricService.stopBusinessTimer(messageId);
            } catch (Exception e) {
                log.warn("WebSocket 비즈니스 로직 처리 시간 측정 완료 중 에러", e);
            }
        }
    }
}
