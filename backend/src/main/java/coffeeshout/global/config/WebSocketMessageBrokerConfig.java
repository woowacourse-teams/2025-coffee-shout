package coffeeshout.global.config;


import coffeeshout.global.interceptor.CustomStompChannelInterceptor;
import coffeeshout.global.interceptor.WebSocketInboundMetricInterceptor;
import coffeeshout.global.interceptor.WebSocketOutboundMetricInterceptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketMessageBrokerConfig implements WebSocketMessageBrokerConfigurer {

    private final TaskScheduler taskScheduler;
    private final CustomStompChannelInterceptor stompChannelInterceptor;
    private final WebSocketInboundMetricInterceptor webSocketInboundMetricInterceptor;
    private final WebSocketOutboundMetricInterceptor webSocketOutboundMetricInterceptor;

    public WebSocketMessageBrokerConfig(
            @Qualifier("webSocketHeartBeatScheduler") TaskScheduler taskScheduler,
            CustomStompChannelInterceptor stompChannelInterceptor,
            WebSocketInboundMetricInterceptor webSocketInboundMetricInterceptor,
            WebSocketOutboundMetricInterceptor webSocketOutboundMetricInterceptor
    ) {
        this.taskScheduler = taskScheduler;
        this.stompChannelInterceptor = stompChannelInterceptor;
        this.webSocketInboundMetricInterceptor = webSocketInboundMetricInterceptor;
        this.webSocketOutboundMetricInterceptor = webSocketOutboundMetricInterceptor;
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
        registration.interceptors(channelInterceptor)
                .taskExecutor()
                .corePoolSize(4)
                .maxPoolSize(12)
                .queueCapacity(200)
                .keepAliveSeconds(60);
        registration.interceptors(webSocketInboundMetricInterceptor, stompChannelInterceptor);
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketOutboundMetricInterceptor);
        registration.interceptors(generateTraceableChannel())
                .taskExecutor()
                .corePoolSize(9)
                .maxPoolSize(18)
                .queueCapacity(500)
                .keepAliveSeconds(30);
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
