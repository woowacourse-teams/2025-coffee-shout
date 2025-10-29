package coffeeshout.global.websocket.infra.handler;

import coffeeshout.global.redis.EventHandler;
import coffeeshout.global.websocket.StompSessionManager;
import coffeeshout.global.websocket.event.player.PlayerReconnectedEvent;
import coffeeshout.global.websocket.event.session.SessionRegisteredEvent;
import coffeeshout.global.websocket.infra.PlayerEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionRegisteredEventHandler implements EventHandler<SessionRegisteredEvent> {

    private final StompSessionManager sessionManager;
    private final PlayerEventPublisher playerEventPublisher;

    @Override
    public void handle(SessionRegisteredEvent event) {
        // 기존 세션이 있으면 재연결 처리
        if (sessionManager.hasPlayerKeyInternal(event.playerKey())) {
            final PlayerReconnectedEvent playerReconnectedEvent = PlayerReconnectedEvent.create(
                    event.playerKey(),
                    event.sessionId()
            );
            playerEventPublisher.publishEvent(playerReconnectedEvent);
        }

        // 모든 인스턴스가 세션 매핑 등록
        sessionManager.registerPlayerSessionInternal(event.playerKey(), event.sessionId());
    }

    @Override
    public Class<SessionRegisteredEvent> eventType() {
        return SessionRegisteredEvent.class;
    }
}
