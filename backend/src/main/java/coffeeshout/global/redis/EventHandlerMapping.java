package coffeeshout.global.redis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EventHandlerMapping {

    private final Map<Class<?>, EventHandler> handlerMap = new HashMap<>();

    public EventHandlerMapping(List<EventHandler> handlers) {
        handlers.forEach(handler -> handlerMap.put(handler.eventType(), handler));
        log.info("이벤트 핸들러 팩토리 초기화: 핸들러 수={}, 지원 타입={}", handlers.size(), handlerMap.keySet());
    }

    public EventHandler getHandler(BaseEvent event) {
        if (!handlerMap.containsKey(event.getClass())) {
            throw new IllegalArgumentException("지원하지 않는 이벤트 타입: " + event.getClass());
        }
        return handlerMap.get(event.getClass());
    }
}
