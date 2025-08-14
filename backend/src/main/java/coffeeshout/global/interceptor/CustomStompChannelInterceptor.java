package coffeeshout.global.interceptor;

import coffeeshout.global.interceptor.handler.StompHandlerRegistry;
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
public class CustomStompChannelInterceptor implements ChannelInterceptor {

    private final StompHandlerRegistry handlerRegistry;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        final StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        final Object commandObj = accessor.getCommand();
        final String sessionId = accessor.getSessionId();

        // STOMP 명령이 아닌 내부 메시지는 무시
        if (!(commandObj instanceof final StompCommand command)) {
            return message;
        }

        try {
            handlerRegistry.getPreSendHandler(command)
                    .ifPresentOrElse(
                            handler -> handler.handle(accessor, sessionId),
                            () -> log.trace("처리할 PreSend 핸들러가 없습니다: command={}", command)
                    );
        } catch (Exception e) {
            log.error("STOMP 인터셉터 처리 중 에러: sessionId={}, command={}", sessionId, command, e);
        }

        return message;
    }

    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        final StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return;
        }

        final Object commandObj = accessor.getCommand();
        final String sessionId = accessor.getSessionId();

        // STOMP 명령이 아닌 내부 메시지는 무시
        if (!(commandObj instanceof final StompCommand command)) {
            return;
        }

        try {
            handlerRegistry.getPostSendHandler(command)
                    .ifPresentOrElse(
                            handler -> handler.handle(accessor, sessionId, sent),
                            () -> log.trace("처리할 PostSend 핸들러가 없습니다: command={}", command)
                    );
        } catch (Exception e) {
            log.error("STOMP postSend 처리 중 에러: sessionId={}, command={}", sessionId, command, e);
        }
    }
}
