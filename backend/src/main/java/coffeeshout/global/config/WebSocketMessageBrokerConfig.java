package coffeeshout.global.config;


import coffeeshout.global.config.properties.RabbitMQProperties;
import coffeeshout.global.config.properties.RabbitMQProperties.Heartbeat;
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
    private final RabbitMQProperties rabbitMQProperties;

    public WebSocketMessageBrokerConfig(
            @Qualifier("webSocketHeartBeatScheduler") TaskScheduler taskScheduler,
            ChannelInterceptor channelInterceptor,
            RabbitMQProperties rabbitMQProperties
    ) {
        this.taskScheduler = taskScheduler;
        this.channelInterceptor = channelInterceptor;
        this.rabbitMQProperties = rabbitMQProperties;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        final Heartbeat heartbeat = rabbitMQProperties.stomp().heartbeat();

        config.enableStompBrokerRelay("/topic/", "/queue/")
                .setRelayHost(rabbitMQProperties.host())
                .setRelayPort(rabbitMQProperties.stomp().port())
                .setClientLogin(rabbitMQProperties.username())
                .setClientPasscode(rabbitMQProperties.password())
                .setSystemLogin(rabbitMQProperties.username())
                .setSystemPasscode(rabbitMQProperties.password())
                .setSystemHeartbeatReceiveInterval(heartbeat.receive())
                .setSystemHeartbeatSendInterval(heartbeat.send())
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
}
