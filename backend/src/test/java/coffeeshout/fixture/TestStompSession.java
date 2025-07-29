package coffeeshout.fixture;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Type;
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

    public <T> MessageCollector<T> subscribe(String subscribeEndpoint, Class<T> payloadClazz) {
        MessageCollector<T> messageCollector = new MessageCollector<>();
        session.subscribe(subscribeEndpoint, new MessageCollectorStompFrameHandler<>(messageCollector, payloadClazz, objectMapper));
        return messageCollector;
    }

    public <T> MessageCollector<T> subscribe(String subscribeEndpoint, TypeReference<T> typeRef) {
        MessageCollector<T> messageCollector = new MessageCollector<>();
        session.subscribe(
                subscribeEndpoint,
                new MessageCollectorStompFrameHandler<>(messageCollector, typeRef, objectMapper)
        );
        return messageCollector;
    }

    public void send(String sendEndpoint, Object bodyMessage) {
        session.send(String.format(sendEndpoint), bodyMessage);
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
            private final Object typeInfo;
            private final ObjectMapper objectMapper;

            public MessageCollectorStompFrameHandler(MessageCollector<T> messageCollector, Class<T> payloadClass, ObjectMapper objectMapper) {
                this.messageCollector = messageCollector;
                this.typeInfo = payloadClass;
                this.objectMapper = objectMapper;
            }

            public MessageCollectorStompFrameHandler(MessageCollector<T> messageCollector, TypeReference<T> typeRef, ObjectMapper objectMapper) {
                this.messageCollector = messageCollector;
                this.typeInfo = typeRef;
                this.objectMapper = objectMapper;
            }

            @Override
            public Type getPayloadType(StompHeaders headers) {
                if (typeInfo instanceof Class) {
                    return (Class<?>) typeInfo;
                } else if (typeInfo instanceof TypeReference) {
                    return ((TypeReference<?>) typeInfo).getType();
                }
                throw new IllegalStateException("Unsupported type info: " + typeInfo);
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                synchronized (messageCollector) {
                    // Always convert using ObjectMapper to ensure proper deserialization of nested objects
                    T convertedPayload;
                    if (typeInfo instanceof Class) {
                        convertedPayload = objectMapper.convertValue(payload, (Class<T>) typeInfo);
                    } else if (typeInfo instanceof TypeReference) {
                        convertedPayload = objectMapper.convertValue(payload, (TypeReference<T>) typeInfo);
                    } else {
                        throw new IllegalStateException("Unsupported type info: " + typeInfo);
                    }
                    messageCollector.add(convertedPayload);
                }
            }
        }
}
