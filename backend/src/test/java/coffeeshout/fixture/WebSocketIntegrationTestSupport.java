package coffeeshout.fixture;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
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
    static final String WEBSOCKET_BASE_URL_FORMAT = "ws://localhost:%d/ws";
    private static final Logger log = LoggerFactory.getLogger(WebSocketIntegrationTestSupport.class);


    @LocalServerPort
    private int port;

    protected TestStompSession createSession() throws InterruptedException, ExecutionException, TimeoutException {
        SockJsClient sockJsClient = new SockJsClient(List.of(
                new WebSocketTransport(new StandardWebSocketClient())
        ));

        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        String url = String.format(WEBSOCKET_BASE_URL_FORMAT, port);
        StompSession session = stompClient
                .connectAsync(
                        url, new StompSessionHandlerAdapter() {

                            @Override
                            public void handleTransportError(StompSession session, Throwable exception) {
                                log.error("STOMP TRANSPORT ERROR: " + exception.getMessage());
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
                                log.error("STOMP EXCEPTION: " + exception.getMessage());
                                throw new RuntimeException(exception);
                            }
                        }
                )
                .get(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        return new TestStompSession(session);
    }
}
