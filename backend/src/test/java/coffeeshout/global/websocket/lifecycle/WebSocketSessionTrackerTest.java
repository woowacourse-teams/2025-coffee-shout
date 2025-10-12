package coffeeshout.global.websocket.lifecycle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import coffeeshout.global.websocket.event.SessionCountChangedEvent;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@ExtendWith(MockitoExtension.class)
class WebSocketSessionTrackerTest {

    @Mock
    ApplicationEventPublisher eventPublisher;

    WebSocketSessionTracker sessionTracker;

    @BeforeEach
    void setUp() {
        sessionTracker = new WebSocketSessionTracker(eventPublisher);
    }

    @Nested
    class 세션_연결 {

        @Test
        void 여러_세션_연결_시_카운트가_순차적으로_증가한다() {
            // given
            SessionConnectEvent event1 = createSessionConnectEvent("session-1");
            SessionConnectEvent event2 = createSessionConnectEvent("session-2");
            SessionConnectEvent event3 = createSessionConnectEvent("session-3");

            // when
            sessionTracker.handleSessionConnected(event1);
            sessionTracker.handleSessionConnected(event2);
            sessionTracker.handleSessionConnected(event3);

            // then
            assertThat(sessionTracker.getActiveSessionCount()).isEqualTo(3);
            then(eventPublisher).should(times(3)).publishEvent(any(SessionCountChangedEvent.class));
        }

        @Test
        void 중복된_세션_ID로_연결_시도_시_카운트가_증가하지_않는다() {
            // given
            String sessionId = "session-1";
            SessionConnectEvent event = createSessionConnectEvent(sessionId);

            // when
            sessionTracker.handleSessionConnected(event);
            sessionTracker.handleSessionConnected(event); // 중복 연결 시도

            // then
            assertThat(sessionTracker.getActiveSessionCount()).isEqualTo(1);
            then(eventPublisher).should(times(1)).publishEvent(any(SessionCountChangedEvent.class));
        }

        @Test
        void sessionId를_추출할_수_없는_경우_세션이_추가되지_않는다() {
            // given
            SessionConnectEvent event = createInvalidSessionConnectEvent();

            // when
            sessionTracker.handleSessionConnected(event);

            // then
            assertThat(sessionTracker.getActiveSessionCount()).isZero();
            then(eventPublisher).should(never()).publishEvent(any());
        }
    }

    @Nested
    class 세션_해제 {

        @Test
        void 여러_세션_중_일부만_해제되면_카운트가_적절히_감소한다() {
            // given
            sessionTracker.handleSessionConnected(createSessionConnectEvent("session-1"));
            sessionTracker.handleSessionConnected(createSessionConnectEvent("session-2"));
            sessionTracker.handleSessionConnected(createSessionConnectEvent("session-3"));

            // when
            sessionTracker.handleSessionDisconnected(createSessionDisconnectEvent("session-2"));

            // then
            assertThat(sessionTracker.getActiveSessionCount()).isEqualTo(2);
        }

        @Test
        void 존재하지_않는_세션_해제_시도_시_카운트가_변하지_않는다() {
            // given
            sessionTracker.handleSessionConnected(createSessionConnectEvent("session-1"));
            int initialCount = sessionTracker.getActiveSessionCount();

            // when
            sessionTracker.handleSessionDisconnected(createSessionDisconnectEvent("non-existent"));

            // then
            assertThat(sessionTracker.getActiveSessionCount()).isEqualTo(initialCount);
        }

        @Test
        void 중복된_세션_해제_시도_시_한번만_처리된다() {
            // given
            String sessionId = "session-1";
            sessionTracker.handleSessionConnected(createSessionConnectEvent(sessionId));
            SessionDisconnectEvent event = createSessionDisconnectEvent(sessionId);

            // when
            sessionTracker.handleSessionDisconnected(event);
            sessionTracker.handleSessionDisconnected(event); // 중복 해제 시도

            // then
            assertThat(sessionTracker.getActiveSessionCount()).isZero();
            // CONNECTED 1번 + DISCONNECTED 1번만
            then(eventPublisher).should(times(2)).publishEvent(any(SessionCountChangedEvent.class));
        }
    }

    @Nested
    class 활성_세션_수_조회 {

        @Test
        void 초기_상태에서_세션_수는_0이다() {
            // when & then
            assertThat(sessionTracker.getActiveSessionCount()).isZero();
        }

        @Test
        void 세션_추가_및_제거_후_정확한_카운트를_반환한다() {
            // given
            sessionTracker.handleSessionConnected(createSessionConnectEvent("session-1"));
            sessionTracker.handleSessionConnected(createSessionConnectEvent("session-2"));
            sessionTracker.handleSessionConnected(createSessionConnectEvent("session-3"));

            // when
            sessionTracker.handleSessionDisconnected(createSessionDisconnectEvent("session-1"));
            sessionTracker.handleSessionDisconnected(createSessionDisconnectEvent("session-2"));

            // then
            assertThat(sessionTracker.getActiveSessionCount()).isEqualTo(1);
        }

        @Test
        void 모든_세션_해제_후_카운트는_0이_된다() {
            // given
            sessionTracker.handleSessionConnected(createSessionConnectEvent("session-1"));
            sessionTracker.handleSessionConnected(createSessionConnectEvent("session-2"));

            // when
            sessionTracker.handleSessionDisconnected(createSessionDisconnectEvent("session-1"));
            sessionTracker.handleSessionDisconnected(createSessionDisconnectEvent("session-2"));

            // then
            assertThat(sessionTracker.getActiveSessionCount()).isZero();
        }
    }

    @Nested
    class 경쟁_조건_테스트 {

        @Test
        void 동시에_여러_세션이_연결되어도_정확한_카운트를_유지한다() throws InterruptedException {
            // given
            int threadCount = 10;
            Thread[] threads = new Thread[threadCount];

            // when
            for (int i = 0; i < threadCount; i++) {
                final int sessionNum = i;
                threads[i] = new Thread(() -> {
                    sessionTracker.handleSessionConnected(
                            createSessionConnectEvent("session-" + sessionNum));
                });
                threads[i].start();
            }

            // 모든 스레드가 완료될 때까지 대기
            for (Thread thread : threads) {
                thread.join();
            }

            // then
            assertThat(sessionTracker.getActiveSessionCount()).isEqualTo(threadCount);
        }

        @Test
        void 동시에_여러_세션이_해제되어도_정확한_카운트를_유지한다() throws InterruptedException {
            // given
            int threadCount = 10;
            for (int i = 0; i < threadCount; i++) {
                sessionTracker.handleSessionConnected(createSessionConnectEvent("session-" + i));
            }

            Thread[] threads = new Thread[threadCount];

            // when
            for (int i = 0; i < threadCount; i++) {
                final int sessionNum = i;
                threads[i] = new Thread(() -> {
                    sessionTracker.handleSessionDisconnected(
                            createSessionDisconnectEvent("session-" + sessionNum));
                });
                threads[i].start();
            }

            // 모든 스레드가 완료될 때까지 대기
            for (Thread thread : threads) {
                thread.join();
            }

            // then
            assertThat(sessionTracker.getActiveSessionCount()).isZero();
        }
    }

    private SessionConnectEvent createSessionConnectEvent(String sessionId) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("simpSessionId", sessionId);
        headers.put(SimpMessageHeaderAccessor.MESSAGE_TYPE_HEADER, SimpMessageType.CONNECT);

        Message<byte[]> message = MessageBuilder.withPayload(new byte[0])
                .copyHeaders(headers)
                .build();

        return new SessionConnectEvent(this, message, null);
    }

    private SessionConnectEvent createInvalidSessionConnectEvent() {
        Map<String, Object> headers = new HashMap<>();
        // simpSessionId 없음

        Message<byte[]> message = MessageBuilder.withPayload(new byte[0])
                .copyHeaders(headers)
                .build();

        return new SessionConnectEvent(this, message, null);
    }

    private SessionDisconnectEvent createSessionDisconnectEvent(String sessionId) {
        Message<byte[]> message = MessageBuilder.withPayload(new byte[0]).build();
        return new SessionDisconnectEvent(this, message, sessionId, CloseStatus.NORMAL, null);
    }
}
