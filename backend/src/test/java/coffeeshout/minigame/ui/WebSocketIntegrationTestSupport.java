package coffeeshout.minigame.ui;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class WebSocketIntegrationTestSupport {

    static final int CONNECT_TIMEOUT_SECONDS = 1;
    static final int DEFAULT_RESPONSE_TIMEOUT_SECONDS = 5;
    static final String WEBSOCKET_BASE_URL_FORMAT = "ws://localhost:%d/ws";

    @LocalServerPort
    private int port;
    private StompSession session;

    @BeforeEach
    void setUp() throws ExecutionException, InterruptedException, TimeoutException {
        SockJsClient sockJsClient = new SockJsClient(List.of(
                new WebSocketTransport(new StandardWebSocketClient())
        ));

        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        String url = String.format(WEBSOCKET_BASE_URL_FORMAT, port);
        session = stompClient
                .connectAsync(
                        url, new StompSessionHandlerAdapter() {

                            @Override
                            public void handleTransportError(StompSession session, Throwable exception) {
                                throw new RuntimeException(exception);
                            }

                            @Override
                            public void handleException(
                                    StompSession session,
                                    StompCommand command,
                                    StompHeaders headers,
                                    byte[] payload,
                                    Throwable exception
                            ) {
                                throw new RuntimeException(exception);
                            }
                        }
                )
                .get(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }


    protected <T> MessageCollector<T> subscribe(String subscribeEndpoint, Class<T> payloadClazz) {
        MessageCollector<T> messageCollector = new MessageCollector<>();
        session.subscribe(subscribeEndpoint, new MessageCollectorStompFrameHandler<>(messageCollector, payloadClazz));
        return messageCollector;
    }

    protected void send(String sendEndpoint, Object bodyMessage) {
        session.send(String.format(sendEndpoint), bodyMessage);
    }

    protected class MessageCollector<T> {
        private final BlockingQueue<T> queue = new LinkedBlockingQueue<>();

        private void add(T message) {
            queue.add(message);
        }

        protected T get() {
            return get(DEFAULT_RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }

        protected T get(long timeout, TimeUnit unit) {
            try {
                T message = null;
                message = queue.poll(timeout, unit);
                if (message == null) {
                    throw new RuntimeException("메시지 수신 대기 시간을 초과했습니다");
                }
                return message;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        protected int size() {
            return queue.size();
        }

        protected boolean isEmpty() {
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

        @SuppressWarnings("unchecked")
        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            messageCollector.add((T) payload);
        }
    }
}
