package coffeeshout.fixture;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;

public class TestStompSession {

    private static final int DEFAULT_RESPONSE_TIMEOUT_SECONDS = 5;

    private final StompSession session;
    private final ObjectMapper objectMapper;

    protected TestStompSession(StompSession session, ObjectMapper objectMapper) {
        this.session = session;
        this.objectMapper = objectMapper;
    }

    public MessageCollector<String> subscribe(String subscribeEndPoint) {
        MessageCollector<String> messageCollector = new MessageCollector<>();
        session.subscribe(
                subscribeEndPoint,
                new MessageCollectorStompFrameHandler<>(messageCollector, String.class)
        );
        return messageCollector;
    }

    public <T> MessageCollector<T> subscribe(String subscribeEndpoint, TypeReference<T> typeRef) {
        MessageCollector<T> messageCollector = new MessageCollector<>();
        session.subscribe(
                subscribeEndpoint,
null
//                new MessageCollectorStompFrameHandler<>(messageCollector, typeRef, objectMapper)
        );
        return messageCollector;
    }

    public void send(String sendEndpoint, Object bodyMessage) {
        session.send(java.lang.String.format(sendEndpoint), bodyMessage);
    }

    public void send(String sendEndpoint, String jsonString) {
        try {
            Object jsonObject = objectMapper.readValue(jsonString, Object.class);
            session.send(sendEndpoint, jsonObject);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 파싱 실패: " + jsonString, e);
        }
    }

    public static class MessageCollector<T> {
        private final BlockingQueue<T> queue = new LinkedBlockingQueue<>();

        private void add(T message) {
            queue.add(message);
        }

        public T get() {
            return get(DEFAULT_RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }

        public T get(long timeout, TimeUnit unit) {
            try {
                T message = queue.poll(timeout, unit);
                if (message == null) {
                    throw new RuntimeException("메시지 수신 대기 시간을 초과했습니다");
                }
                return message;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        public int size() {
            return queue.size();
        }

        public boolean isEmpty() {
            return queue.isEmpty();
        }
    }

    @SuppressWarnings("unchecked")
    private static class MessageCollectorStompFrameHandler<T> implements StompFrameHandler {
        private final MessageCollector<T> messageCollector;
        private final Class<T> payloadClass;

        public MessageCollectorStompFrameHandler(
                MessageCollector<T> messageCollector,
                Class<T> payloadClass
        ) {
            this.messageCollector = messageCollector;
            this.payloadClass = payloadClass;
        }

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return byte[].class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            synchronized (messageCollector) {
                try {
                    if (payloadClass == String.class && payload != null) {
                        String jsonString = new String((byte[]) payload, StandardCharsets.UTF_8);
                        messageCollector.add((T) jsonString);
                    } else {
                        messageCollector.add((T) payload);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("메시지 변환 실패: " + payload, e);
                }
            }
        }
    }
}
