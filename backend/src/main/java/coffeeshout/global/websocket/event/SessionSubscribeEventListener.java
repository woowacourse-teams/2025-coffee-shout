package coffeeshout.global.websocket.event;

import coffeeshout.global.websocket.StompSessionManager;
import coffeeshout.global.websocket.SubscriptionInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionSubscribeEventListener {

    private final StompSessionManager sessionManager;
    private final SubscriptionInfoService subscriptionInfoService;

    @EventListener
    public void handleSessionSubscribe(SessionSubscribeEvent event) {
        final String sessionId = event.getMessage().getHeaders().get("simpSessionId", String.class);
        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        final String destination = accessor.getDestination();
        final String subscriptionId = accessor.getSubscriptionId();
        
        // 플레이어 정보 가져오기 (있으면)
        String playerInfo = "";
        if (sessionManager.hasPlayerKey(sessionId)) {
            final String playerKey = sessionManager.getPlayerKey(sessionId);
            final String joinCode = sessionManager.extractJoinCode(playerKey);
            final String playerName = sessionManager.extractPlayerName(playerKey);
            playerInfo = String.format(", joinCode=%s, playerName=%s", joinCode, playerName);
        }
        
        log.info("구독 시작: sessionId={}, destination={}, subscriptionId={}{}", 
                sessionId, destination, subscriptionId, playerInfo);
                
        // INFO 레벨에서도 상세 구독 정보 로깅 (구독자 수 포함)
        subscriptionInfoService.logSubscriptionInfo(destination);
    }

    @EventListener
    public void handleSessionUnsubscribe(SessionUnsubscribeEvent event) {
        final String sessionId = event.getMessage().getHeaders().get("simpSessionId", String.class);
        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        final String subscriptionId = accessor.getSubscriptionId();
        
        // 플레이어 정보 가져오기 (있으면)
        String playerInfo = "";
        if (sessionManager.hasPlayerKey(sessionId)) {
            final String playerKey = sessionManager.getPlayerKey(sessionId);
            final String joinCode = sessionManager.extractJoinCode(playerKey);
            final String playerName = sessionManager.extractPlayerName(playerKey);
            playerInfo = String.format(", joinCode=%s, playerName=%s", joinCode, playerName);
        }
        
        log.info("구독 해제: sessionId={}, subscriptionId={}{}", 
                sessionId, subscriptionId, playerInfo);
    }
}
