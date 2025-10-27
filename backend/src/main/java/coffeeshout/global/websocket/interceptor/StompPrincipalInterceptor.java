package coffeeshout.global.websocket.interceptor;

import coffeeshout.global.domain.PlayerPrincipal;
import coffeeshout.global.websocket.SynchronizedWebsocketInfo;
import java.util.Objects;
import java.util.UUID;
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
public class StompPrincipalInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        final StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        if (accessor.getCommand() == StompCommand.CONNECT) {
            registerPrincipal(accessor);
        }

        return message;
    }

    private void registerPrincipal(StompHeaderAccessor accessor) {
        final String playerName = accessor.getFirstNativeHeader("playerName");
        final String joinCode = accessor.getFirstNativeHeader("joinCode");

        log.debug("STOMP CONNECT - playerName: {}, joinCode: {}", playerName, joinCode);

        final String sessionId = UUID.randomUUID().toString();

        final PlayerPrincipal principal = new PlayerPrincipal(sessionId, playerName, joinCode);
        accessor.setUser(principal);
    }
}
