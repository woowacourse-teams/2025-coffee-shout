package coffeeshout.fixture;

import com.fasterxml.jackson.core.type.TypeReference;
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

    protected TestStompSession(StompSession session) {
        this.session = session;
    }

    public <T> MessageCollector<T> subscribe(String subscribeEndpoint, Class<T> payloadClazz) {
        MessageCollector<T> messageCollector = new MessageCollector<>();
        session.subscribe(subscribeEndpoint, new MessageCollectorStompFrameHandler<>(messageCollector, payloadClazz));
        return messageCollector;
    }

    public <T> MessageCollector<T> subscribe(String subscribeEndpoint, TypeReference<T> typeRef) {
        MessageCollector<T> messageCollector = new MessageCollector<>();
        session.subscribe(
                subscribeEndpoint,
                new MessageCollectorStompFrameHandler<>(messageCollector, typeRef.getType())
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
    private class MessageCollectorStompFrameHandler<T> implements StompFrameHandler {

        private final Type payloadType;
        private final MessageCollector<T> messageCollector;

        MessageCollectorStompFrameHandler(MessageCollector<T> messageCollector, Type payloadType) {
            this.messageCollector = messageCollector;
            this.payloadType = payloadType;
        }

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return payloadType;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            synchronized (messageCollector) {
                messageCollector.add((T) payload);
            }
        }
    }
}
