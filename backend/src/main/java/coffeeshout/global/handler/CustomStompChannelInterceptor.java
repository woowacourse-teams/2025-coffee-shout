package coffeeshout.global.handler;

import coffeeshout.global.metric.WebSocketMetricService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomStompChannelInterceptor implements ChannelInterceptor {

    private final WebSocketMetricService webSocketMetricService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) return message;

        StompCommand command = accessor.getCommand();
        String sessionId = accessor.getSessionId();

        // command null 체크 추가
        if (command == null) {
            // 내부 메시지거나 하트비트 같은 애들
            return message;
        }

        try {
            switch (command) {
                case CONNECT:
                    handleConnect(accessor);
                    break;

                case CONNECTED:
                    handleConnected(sessionId);
                    break;

                case SUBSCRIBE:
                    log.info("구독 요청: sessionId={}, destination={}", sessionId, accessor.getDestination());
                    break;

                case UNSUBSCRIBE:
                    log.info("구독 해제: sessionId={}", sessionId);
                    break;

                case SEND:
                    log.info("메시지 전송: sessionId={}, destination={}", sessionId, accessor.getDestination());
                    break;

                case DISCONNECT:
                    handleDisconnect(sessionId, accessor);
                    break;

                case ERROR:
                    handleError(sessionId, accessor);
                    break;

                default:
                    break;
            }
        } catch (Exception e) {
            log.error("인터셉터 처리 중 에러: sessionId={}, command={}", sessionId, command, e);
        }
        return message;
    }

    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        if (!sent) {
            // 메시지 전송 실패했을 때 처리
            StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
            if (accessor != null) {
                log.warn("메시지 전송 실패: sessionId={}, command={}",
                        accessor.getSessionId(), accessor.getCommand());
            }
        }
    }

    @Override
    public Message<?> postReceive(Message<?> message, MessageChannel channel) {
        return message;
    }

    @Override
    public boolean preReceive(MessageChannel channel) {
        return true;
    }

    private void handleConnect(StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        log.info("STOMP 연결 시도: sessionId={}", sessionId);

        // 연결 ID를 세션 속성에서 가져오기 (HandshakeInterceptor에서 설정했다고 가정)
        String connectionId = (String) accessor.getSessionAttributes().get("connectionId");
        if (connectionId != null) {
            // 연결 시작 시간 기록은 HandshakeInterceptor에서 이미 했다고 가정
            log.debug("연결 ID 확인: connectionId={}", connectionId);
        }
    }

    private void handleConnected(String sessionId) {
        log.info("STOMP 연결 완료: sessionId={}", sessionId);

        // 여기서는 sessionAttributes에서 connectionId 가져올 수 없어서
        // sessionId를 connectionId로 사용하거나 다른 방법 필요
        webSocketMetricService.completeConnection(sessionId);
    }

    private void handleDisconnect(String sessionId, StompHeaderAccessor accessor) {
        log.info("STOMP 연결 해제: sessionId={}", sessionId);

        // 정상 종료로 간주 (STOMP의 경우 클라이언트가 명시적으로 DISCONNECT 보냄)
        webSocketMetricService.recordDisconnection(sessionId, "Normal disconnect", true);
    }

    private void handleError(String sessionId, StompHeaderAccessor accessor) {
        String errorMessage = accessor.getMessage();
        log.error("STOMP 에러 발생: sessionId={}, message={}", sessionId, errorMessage);

        // 에러로 인한 비정상 종료
        webSocketMetricService.recordDisconnection(sessionId, errorMessage, false);
    }
}
