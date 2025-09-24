package coffeeshout.global.config;


import coffeeshout.global.interceptor.WebSocketInboundMetricInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketMessageBrokerConfig implements WebSocketMessageBrokerConfigurer {

    private final TaskScheduler taskScheduler;
    private final WebSocketInboundMetricInterceptor webSocketInboundMetricInterceptor;

    public WebSocketMessageBrokerConfig(
            TaskScheduler taskScheduler,
            WebSocketInboundMetricInterceptor webSocketInboundMetricInterceptor
    ) {
        this.taskScheduler = taskScheduler;
        this.webSocketInboundMetricInterceptor = webSocketInboundMetricInterceptor;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        ThreadPoolTaskScheduler heartbeatScheduler = new ThreadPoolTaskScheduler();
        heartbeatScheduler.setPoolSize(1);
        heartbeatScheduler.setThreadNamePrefix("wss-heartbeat-thread-");
        heartbeatScheduler.initialize();

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
        registration.interceptors(webSocketInboundMetricInterceptor)
                .taskExecutor()
                .corePoolSize(8)
                .maxPoolSize(16)
                .queueCapacity(8192)
                .keepAliveSeconds(60);
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.interceptors()
                .taskExecutor()
                .corePoolSize(64)
                .maxPoolSize(128)
                .queueCapacity(16384)
                .keepAliveSeconds(60);
    }
}
