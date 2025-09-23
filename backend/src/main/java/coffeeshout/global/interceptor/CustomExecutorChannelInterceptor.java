package coffeeshout.global.interceptor;

import coffeeshout.global.trace.SpanRepository;
import java.util.UUID;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.support.ExecutorChannelInterceptor;

public class CustomExecutorChannelInterceptor implements ExecutorChannelInterceptor {

    @Override
    public Message<?> beforeHandle(Message<?> message, MessageChannel channel, MessageHandler handler) {
        return message;
    }

    @Override
    public void afterMessageHandled(Message<?> message, MessageChannel channel, MessageHandler handler, Exception exception) {
        if (SimpMessageType.HEARTBEAT.equals(message.getHeaders().get("simpMessageType"))) {
            return;
        }
        final UUID uuid = (UUID) message.getHeaders().get("otelSpan");
        SpanRepository.endSpan(uuid, exception);
    }
}
