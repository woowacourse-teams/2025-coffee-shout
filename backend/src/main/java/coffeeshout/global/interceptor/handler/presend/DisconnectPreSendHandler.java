package coffeeshout.global.interceptor.handler.presend;

import coffeeshout.global.interceptor.handler.PreSendHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DisconnectPreSendHandler implements PreSendHandler {

    @Override
    public StompCommand getCommand() {
        return StompCommand.DISCONNECT;
    }

    @Override
    public void handle(StompHeaderAccessor accessor, String sessionId) {
        log.info("WebSocket 연결 해제 요청: sessionId={}", sessionId);
    }
}