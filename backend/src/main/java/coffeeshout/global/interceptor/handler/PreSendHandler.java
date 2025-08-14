package coffeeshout.global.interceptor.handler;

import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

/**
 * STOMP PreSend 명령 처리 인터페이스
 */
public interface PreSendHandler {
    
    /**
     * 처리할 STOMP 명령
     */
    StompCommand getCommand();
    
    /**
     * PreSend 명령 처리
     */
    void handle(StompHeaderAccessor accessor, String sessionId);
}