package coffeeshout.global.config;


import coffeeshout.global.interceptor.WebSocketInboundMetricInterceptor;
import coffeeshout.global.interceptor.WebSocketOutboundMetricInterceptor;
import io.micrometer.context.ContextSnapshot;
import io.micrometer.context.ContextSnapshotFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketMessageBrokerConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketInboundMetricInterceptor webSocketInboundMetricInterceptor;
    private final WebSocketOutboundMetricInterceptor webSocketOutboundMetricInterceptor;

    public WebSocketMessageBrokerConfig(
            WebSocketInboundMetricInterceptor webSocketInboundMetricInterceptor,
            WebSocketOutboundMetricInterceptor webSocketOutboundMetricInterceptor
    ) {
        this.webSocketInboundMetricInterceptor = webSocketInboundMetricInterceptor;
        this.webSocketOutboundMetricInterceptor = webSocketOutboundMetricInterceptor;
    }

    @Bean(name = "webSocketHeartbeatScheduler")
    public ThreadPoolTaskScheduler webSocketHeartbeatScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("wss-heartbeat-thread-");
        scheduler.setTaskDecorator(runnable -> {
            return () -> {
                log.info("하트비트 발송");
                runnable.run();
            };
        });
        scheduler.initialize();
        return scheduler;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic/", "/queue/")
                .setHeartbeatValue(new long[]{4000, 4000})
                .setTaskScheduler(webSocketHeartbeatScheduler());

        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Bean(name = "webSocketInboundExecutor")
    public ThreadPoolTaskExecutor webSocketInboundExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("inbound-");
        executor.setCorePoolSize(32);
        executor.setMaxPoolSize(32);
        executor.setQueueCapacity(Integer.MAX_VALUE);
        executor.initialize();
        return executor;
    }

    @Bean(name = "webSocketOutboundExecutor")
    public ThreadPoolTaskExecutor webSocketOutboundExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("outbound-");
        executor.setTaskDecorator(runnable -> {
            final ContextSnapshot snapshot = ContextSnapshotFactory.builder().build().captureAll();
            return snapshot.wrap(runnable);
        });
        executor.setCorePoolSize(16);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(Integer.MAX_VALUE);
        executor.initialize();
        return executor;
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketInboundMetricInterceptor);
        registration.taskExecutor(webSocketInboundExecutor());
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketOutboundMetricInterceptor);
        registration.taskExecutor(webSocketOutboundExecutor());
    }
}
