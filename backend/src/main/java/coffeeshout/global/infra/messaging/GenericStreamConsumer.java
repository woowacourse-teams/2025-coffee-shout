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
 * Redis Stream 이벤트를 소비하는 제너릭 Consumer
 * <p>
 * 메시징 인프라(메시지 수신, 파싱, 에러 처리)를 담당하고,
 * 비즈니스 로직은 {@link StreamEventHandler}에 위임합니다.
 * </p>
 *
 * <p><b>책임:</b></p>
 * <ul>
 *   <li>Redis Stream 리스너 등록</li>
 *   <li>메시지 수신 및 파싱</li>
 *   <li>Handler에 비즈니스 로직 위임</li>
 *   <li>공통 에러 처리 및 로깅</li>
 * </ul>
 *
 * <p><b>사용 방법:</b></p>
 * <pre>
 * // Configuration에서 빈 등록
 * &#64;Bean
 * public GenericStreamConsumer&lt;RoomJoinEvent&gt; roomJoinConsumer(
 *     RoomJoinEventHandler handler,
 *     &#64;Qualifier("roomEnterStreamContainer") StreamMessageListenerContainer container,
 *     RedisStreamProperties properties,
 *     ObjectMapper objectMapper
 * ) {
 *     return new GenericStreamConsumer&lt;&gt;(
 *         handler,
 *         RoomJoinEvent.class,
 *         container,
 *         properties.roomJoinKey(),
 *         "방 입장",
 *         objectMapper
 *     );
 * }
 * </pre>
 *
 * @param <T> 처리할 이벤트 타입
 * @see StreamEventHandler
 */
@Slf4j
public class GenericStreamConsumer<T> implements StreamListener<String, ObjectRecord<String, String>> {

    private final StreamEventHandler<T> handler;
    private final Class<T> eventType;
    private final StreamMessageListenerContainer<String, ObjectRecord<String, String>> container;
    private final String streamKey;
    private final String eventName;
    private final ObjectMapper objectMapper;

    /**
     * GenericStreamConsumer 생성자
     *
     * @param handler     비즈니스 로직을 처리할 Handler
     * @param eventType   이벤트 타입 (역직렬화용)
     * @param container   Redis Stream 리스너 컨테이너
     * @param streamKey   Redis Stream 키
     * @param eventName   로깅용 이벤트 이름 (예: "방 입장")
     * @param objectMapper JSON 변환용 ObjectMapper
     */
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

    /**
     * Redis Stream 리스너를 등록합니다.
     * <p>
     * 애플리케이션 시작 시 자동으로 호출되어 Stream 구독을 시작합니다.
     * </p>
     */
    @PostConstruct
    public void registerListener() {
        container.receive(StreamOffset.fromStart(streamKey), this);
        log.info("{} 이벤트 스트림 리스너 등록 완료: streamKey={}", eventName, streamKey);
    }

    /**
     * Redis Stream 메시지를 수신하고 처리합니다.
     * <p>
     * 메시지를 파싱하여 Handler에 위임하고, 발생한 예외를 처리합니다.
     * </p>
     *
     * @param message 수신한 Redis Stream 메시지
     */
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

    /**
     * Redis Stream 메시지를 이벤트 객체로 파싱합니다.
     *
     * @param message Redis Stream 메시지
     * @return 파싱된 이벤트 객체
     * @throws IllegalArgumentException JSON 파싱 실패 시
     */
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
