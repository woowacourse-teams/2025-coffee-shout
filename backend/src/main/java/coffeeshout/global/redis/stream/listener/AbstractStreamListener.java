package coffeeshout.global.redis.stream.listener;

import coffeeshout.global.redis.BaseEvent;
import coffeeshout.global.redis.stream.EventHandlerFacade;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractStreamListener implements StreamListener<String, ObjectRecord<String, String>> {

    private final StreamMessageListenerContainer<String, ObjectRecord<String, String>> streamContainer;
    private final ObjectMapper objectMapper;
    private final EventHandlerFacade eventHandlerFacade;

    @PostConstruct
    public void registerListener() {
        streamContainer.receive(configStreamOffset(), this);
    }

    @Override
    public void onMessage(ObjectRecord<String, String> message) {
        eventHandlerFacade.handle(convertEvent(message));
    }

    private BaseEvent convertEvent(ObjectRecord<String, String> message) {
        try {
            return objectMapper.readValue(message.getValue(), BaseEvent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract StreamOffset<String> configStreamOffset();
}
