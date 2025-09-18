package coffeeshout.room.infra.handler;

import coffeeshout.room.domain.event.BaseEvent;
import coffeeshout.room.domain.event.RoomEventType;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RoomEventHandlerFactory {

    private final Map<RoomEventType, RoomEventHandler<? extends BaseEvent>> handlerMap;

    public RoomEventHandlerFactory(List<RoomEventHandler<? extends BaseEvent>> handlers) {
        this.handlerMap = handlers.stream()
                .collect(Collectors.toMap(
                        RoomEventHandler::getSupportedEventType,
                        Function.identity()
                ));
        
        log.info("이벤트 핸들러 팩토리 초기화: 핸들러 수={}, 지원 타입={}", 
                handlers.size(), handlerMap.keySet());
    }

    @SuppressWarnings("unchecked")
    public <T extends BaseEvent> RoomEventHandler<T> getHandler(RoomEventType eventType) {
        final RoomEventHandler<? extends BaseEvent> handler = handlerMap.get(eventType);
        
        if (handler == null) {
            throw new IllegalArgumentException("지원하지 않는 이벤트 타입: " + eventType);
        }
        
        return (RoomEventHandler<T>) handler;
    }

    public boolean canHandle(RoomEventType eventType) {
        return handlerMap.containsKey(eventType);
    }
}
