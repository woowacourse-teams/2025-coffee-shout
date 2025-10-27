package coffeeshout.global.redis.stream;

import coffeeshout.global.redis.BaseEvent;
import coffeeshout.global.redis.EventHandler;
import coffeeshout.global.redis.EventHandlerMapping;
import coffeeshout.global.redis.UserEvent;
import coffeeshout.global.trace.Traceable;
import coffeeshout.global.trace.TracerProvider;
import coffeeshout.global.websocket.LoggingSimpMessagingTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventHandlerExecutor {

    private final EventHandlerMapping handlerFactory;
    private final TracerProvider tracerProvider;
    private final LoggingSimpMessagingTemplate loggingSimpMessagingTemplate;

    public void handle(BaseEvent event) {
        try {
            final EventHandler handler = handlerFactory.getHandler(event);
            final Runnable handling = () -> handler.handle(event);

            if (event instanceof Traceable traceable) {
                tracerProvider.executeWithTraceContext(traceable.traceInfo(), handling, event);
                return;
            }
            handling.run();

        } catch (Exception e) {
            log.error("이벤트 처리 실패: message={}", event, e);
            if (event instanceof UserEvent userEvent) {
                loggingSimpMessagingTemplate.convertAndSendError(userEvent.userName(), e);
            }
        }
    }

}
