package coffeeshout.global.websocket.event;

import coffeeshout.global.metric.WebSocketMetricService;
import coffeeshout.global.websocket.DelayedPlayerRemovalService;
import coffeeshout.global.websocket.StompSessionManager;
import coffeeshout.global.websocket.SubscriptionInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionDisconnectEventListener {

    private static final String CLIENT_DISCONNECT = "CLIENT_DISCONNECT";

    private final StompSessionManager sessionManager;
    private final DelayedPlayerRemovalService delayedPlayerRemovalService;
    private final SubscriptionInfoService subscriptionInfoService;
    private final WebSocketMetricService webSocketMetricService;

    @EventListener
    public void handleSessionDisconnectEvent(SessionDisconnectEvent event) {
        final String sessionId = event.getSessionId();
        final CloseStatus closeStatus = event.getCloseStatus();

        log.info("세션 연결 해제 감지: sessionId={}, closeStatus={}, reason={}",
                sessionId, closeStatus, closeStatus.getReason());

        // 구독 정보 정리
        subscriptionInfoService.removeAllSubscriptions(sessionId);

        // 중복 처리 방지
        if (sessionManager.isDisconnectionProcessed(sessionId)) {
            log.debug("이미 처리된 연결 해제 무시: sessionId={}", sessionId);
            return;
        }

        log.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!! 야 감지시작했다! {}", sessionManager.hasPlayerKey(sessionId));
        // 플레이어 세션인지 확인
        if (sessionManager.hasPlayerKey(sessionId)) {
            log.info("이 놈 감지됐다!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            final String playerKey = sessionManager.getPlayerKey(sessionId);
            log.info("플레이어 세션 해제 감지: playerKey={}, sessionId={}", playerKey, sessionId);

            // 지연 삭제 스케줄링
            delayedPlayerRemovalService.schedulePlayerRemoval(playerKey, sessionId, "SESSION_DISCONNECT");
        }
        log.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! 감지 끝났다.");

        webSocketMetricService.recordDisconnection(sessionId, CLIENT_DISCONNECT);
    }
}
