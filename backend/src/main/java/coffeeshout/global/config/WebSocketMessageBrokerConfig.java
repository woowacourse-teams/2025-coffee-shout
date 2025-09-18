package coffeeshout.global.config;


import coffeeshout.global.interceptor.CustomExecutorChannelInterceptor;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketMessageBrokerConfig implements WebSocketMessageBrokerConfigurer {

    private final TaskScheduler taskScheduler;
    private final ChannelInterceptor channelInterceptor;
    private final Tracer tracer;


    public WebSocketMessageBrokerConfig(
            @Qualifier("webSocketHeartBeatScheduler") TaskScheduler taskScheduler,
            ChannelInterceptor channelInterceptor,
            Tracer tracer
    ) {
        this.taskScheduler = taskScheduler;
        this.channelInterceptor = channelInterceptor;
        this.tracer = tracer;
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
        registration.interceptors(new CustomExecutorChannelInterceptor(tracer));
    }
}
