package coffeeshout.global.infra.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

/**
 * 제너릭 Stream Consumer - 메시징 인프라 담당
 * 메시지 수신/파싱/에러처리 후 Handler에 비즈니스 로직 위임
 */
@Slf4j
public class GenericStreamConsumer<T> implements StreamListener<String, ObjectRecord<String, String>> {

    private final StreamEventHandler<T> handler;
    private final Class<T> eventType;
    private final StreamMessageListenerContainer<String, ObjectRecord<String, String>> container;
    private final String streamKey;
    private final String eventName;
    private final ObjectMapper objectMapper;

    public GenericStreamConsumer(
            StreamEventHandler<T> handler,
            Class<T> eventType,
            StreamMessageListenerContainer<String, ObjectRecord<String, String>> container,
            String streamKey,
            String eventName,
            ObjectMapper objectMapper
    ) {
        this.handler = handler;
        this.eventType = eventType;
        this.container = container;
        this.streamKey = streamKey;
        this.eventName = eventName;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void registerListener() {
        container.receive(StreamOffset.fromStart(streamKey), this);
        log.info("{} 이벤트 스트림 리스너 등록 완료: streamKey={}", eventName, streamKey);
    }

    @Override
    public void onMessage(ObjectRecord<String, String> message) {
        log.info("{} 이벤트 메시지 수신: messageId={}, streamKey={}",
                eventName, message.getId(), streamKey);

        try {
            T event = parseEvent(message);
            log.debug("{} 이벤트 파싱 완료: event={}", eventName, event);

            handler.handle(event);

            log.info("{} 이벤트 처리 성공: messageId={}", eventName, message.getId());

        } catch (IllegalArgumentException e) {
            // 파싱 실패 - 메시지 형식 오류
            log.error("{} 이벤트 파싱 실패: messageId={}, error={}",
                    eventName, message.getId(), e.getMessage(), e);
        } catch (Exception e) {
            // 비즈니스 로직 실패
            log.error("{} 이벤트 처리 실패: messageId={}, error={}",
                    eventName, message.getId(), e.getMessage(), e);
            // TODO: 재시도 로직, DLQ(Dead Letter Queue) 전송 등
        }
    }

    private T parseEvent(ObjectRecord<String, String> message) {
        try {
            String value = message.getValue();
            return objectMapper.readValue(value, eventType);
        } catch (JsonProcessingException e) {
            log.error("{} 이벤트 JSON 파싱 실패: messageId={}, messageValue={}, eventType={}",
                    eventName, message.getId(), message.getValue(), eventType.getSimpleName(), e);
            throw new IllegalArgumentException(
                    String.format("%s 이벤트 파싱 실패: %s", eventName, e.getMessage()), e);
        } catch (Exception e) {
            log.error("{} 이벤트 파싱 중 예외 발생: messageId={}",
                    eventName, message.getId(), e);
            throw new IllegalArgumentException(
                    String.format("%s 이벤트 파싱 실패: %s", eventName, e.getMessage()), e);
        }
    }
}
