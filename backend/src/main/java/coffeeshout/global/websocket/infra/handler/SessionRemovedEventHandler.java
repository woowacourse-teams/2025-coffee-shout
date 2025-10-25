package coffeeshout.global.websocket.infra.handler;

import coffeeshout.global.redis.BaseEvent;
import coffeeshout.global.redis.EventHandler;
import coffeeshout.global.websocket.StompSessionManager;
import coffeeshout.global.websocket.event.session.SessionRemovedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionRemovedEventHandler implements EventHandler {

    private final StompSessionManager sessionManager;

    @Override
    public void handle(BaseEvent event) {
        final SessionRemovedEvent sessionRemovedEvent = (SessionRemovedEvent) event;
        sessionManager.removeSessionInternal(sessionRemovedEvent.sessionId());
    }

    @Override
    public Class<?> eventType() {
        return SessionRemovedEvent.class;
    }
}
