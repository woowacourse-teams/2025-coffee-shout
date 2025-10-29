package coffeeshout.global.redis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EventHandlerMapping {

    private final Map<Class<? extends BaseEvent>, EventHandler<? extends BaseEvent>> handlerMap = new HashMap<>();

    public EventHandlerMapping(List<EventHandler<? extends BaseEvent>> handlers) {
        handlers.forEach(handler -> handlerMap.put(handler.eventType(), handler));
    }

    public <T extends BaseEvent> EventHandler<T> getHandler(T event) {
        if (!handlerMap.containsKey(event.getClass())) {
            throw new IllegalArgumentException("지원하지 않는 이벤트 타입: " + event.getClass());
        }
        //noinspection unchecked
        return (EventHandler<T>) handlerMap.get(event.getClass());
    }
}
