package coffeeshout.global.config;


import coffeeshout.global.trace.SpanRepository;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.ExecutorChannelInterceptor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketMessageBrokerConfig implements WebSocketMessageBrokerConfigurer {

    private final TaskScheduler taskScheduler;
    private final ChannelInterceptor channelInterceptor;

    public WebSocketMessageBrokerConfig(
            @Qualifier("webSocketHeartBeatScheduler") TaskScheduler taskScheduler,
            ChannelInterceptor channelInterceptor
    ) {
        this.taskScheduler = taskScheduler;
        this.channelInterceptor = channelInterceptor;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic/", "/queue/")
                .setHeartbeatValue(new long[]{4000, 4000})
                .setTaskScheduler(taskScheduler);

        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(channelInterceptor);
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.interceptors(generateTraceableChannel());
    }

    private static ExecutorChannelInterceptor generateTraceableChannel() {
        return new ExecutorChannelInterceptor() {
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
                if (SimpMessageType.HEARTBEAT.equals(message.getHeaders().get("simpMessageType"))) {
                    return;
                }
                final Object uuidObj = message.getHeaders().get("otelSpan");
                if (uuidObj instanceof UUID) {
                    SpanRepository.endSpan((UUID) uuidObj, exception);
                }
            }
        };
    }
}
