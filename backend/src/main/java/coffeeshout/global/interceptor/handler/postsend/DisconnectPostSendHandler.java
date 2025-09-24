package coffeeshout.global.interceptor.handler.postsend;

import coffeeshout.global.interceptor.handler.PostSendHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DisconnectPostSendHandler implements PostSendHandler {

    @Override
    public StompCommand getCommand() {
        return StompCommand.DISCONNECT;
    }

    @Override
    public void handle(StompHeaderAccessor accessor, String sessionId, boolean sent) {
        if (!sent) {
            return;
        }

        // DISCONNECT 프레임 수신 로깅만
        log.info("클라이언트 DISCONNECT 프레임 수신: sessionId={}", sessionId);
    }
}
