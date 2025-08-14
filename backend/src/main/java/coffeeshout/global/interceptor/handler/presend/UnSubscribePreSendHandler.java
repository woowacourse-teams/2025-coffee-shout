package coffeeshout.global.interceptor.handler.presend;

import coffeeshout.global.interceptor.handler.PreSendHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UnSubscribePreSendHandler implements PreSendHandler {

    @Override
    public StompCommand getCommand() {
        return StompCommand.UNSUBSCRIBE;
    }

    @Override
    public void handle(StompHeaderAccessor accessor, String sessionId) {
        log.debug("구독 해제: sessionId={}", sessionId);
    }
}
