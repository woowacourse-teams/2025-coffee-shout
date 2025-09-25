package coffeeshout.global.config;


import coffeeshout.global.interceptor.WebSocketInboundMetricInterceptor;
import io.micrometer.context.ContextSnapshot;
import io.micrometer.context.ContextSnapshotFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketMessageBrokerConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketInboundMetricInterceptor webSocketInboundMetricInterceptor;

    public WebSocketMessageBrokerConfig(
            WebSocketInboundMetricInterceptor webSocketInboundMetricInterceptor
    ) {
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
                .setTaskScheduler(heartbeatScheduler);

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
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("outbound-");
        executor.setTaskDecorator(runnable -> {
            final ContextSnapshot snapshot = ContextSnapshotFactory.builder().build().captureAll();
            return snapshot.wrap(runnable);
        });
        executor.setCorePoolSize(64);
        executor.setMaxPoolSize(128);
        executor.setQueueCapacity(16384);
        executor.setKeepAliveSeconds(60);
        executor.initialize();
        registration.taskExecutor(executor);
    }
}
