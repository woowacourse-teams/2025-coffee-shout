package coffeeshout.global.config;

import coffeeshout.global.websocket.SynchronizedWebsocketInfo;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.support.ExecutorChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
public class MessageMappingExecutorChannelInterceptor implements ExecutorChannelInterceptor {

    @Override
    public Message<?> beforeHandle(Message<?> message, MessageChannel channel, MessageHandler handler) {
        SynchronizedWebsocketInfo.bindWebsocketInfo(message);
        return message;
    }
}
