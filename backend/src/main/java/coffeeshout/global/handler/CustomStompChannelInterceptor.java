package coffeeshout.global.handler;

import java.util.Set;

import coffeeshout.global.metric.WebSocketMetricService;
import java.util.concurrent.ConcurrentHashMap;
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

    // 중복 처리 방지용
    private final Set<String> processedDisconnections = ConcurrentHashMap.newKeySet();

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) return message;

        Object commandObj = accessor.getCommand();
        String sessionId = accessor.getSessionId();

        // STOMP 명령이 아닌 내부 메시지는 무시
        if (!(commandObj instanceof StompCommand)) {
            return message;
        }

        StompCommand command = (StompCommand) commandObj;

        try {
            switch (command) {
                case CONNECT:
                    log.info("WebSocket 연결 시작: sessionId={}", sessionId);
                    webSocketMetricService.startConnection(sessionId);
                    break;

                case SUBSCRIBE:
                    log.debug("구독 요청: sessionId={}, destination={}", sessionId, accessor.getDestination());
                    break;

                case UNSUBSCRIBE:
                    log.debug("구독 해제: sessionId={}", sessionId);
                    break;

                case SEND:
                    log.debug("클라이언트 메시지: sessionId={}, destination={}", sessionId, accessor.getDestination());
                    break;

                case DISCONNECT:
                    log.info("WebSocket 연결 해제 요청: sessionId={}", sessionId);
                    // DISCONNECT는 postSend에서만 처리하도록 변경
                    break;

                case ERROR:
                    String errorMessage = accessor.getMessage();
                    log.error("STOMP 에러: sessionId={}, message={}", sessionId, errorMessage);
                    webSocketMetricService.recordDisconnection(sessionId, "stomp_error", false);
                    break;

                default:
                    log.trace("기타 STOMP 명령: sessionId={}, command={}", sessionId, command);
                    break;
            }
        } catch (Exception e) {
            log.error("STOMP 인터셉터 처리 중 에러: sessionId={}, command={}", sessionId, command, e);
        }

        return message;
    }

    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) return;

        Object commandObj = accessor.getCommand();
        String sessionId = accessor.getSessionId();

        // STOMP 명령이 아닌 내부 메시지는 무시
        if (!(commandObj instanceof StompCommand)) {
            return;
        }

        StompCommand command = (StompCommand) commandObj;

        try {
            if (command == StompCommand.CONNECT && sent) {
                // 서버에서 CONNECTED 응답을 성공적으로 보냈을 때 연결 완료 처리
                log.info("WebSocket 연결 완료: sessionId={}", sessionId);
                webSocketMetricService.completeConnection(sessionId);
            } else if (command == StompCommand.DISCONNECT && sent) {
                // DISCONNECT 메시지 전송 완료 시 연결 해제 처리 (중복 방지)
                if (processedDisconnections.add(sessionId)) {
                    log.info("WebSocket 연결 해제 완료: sessionId={}", sessionId);
                    webSocketMetricService.recordDisconnection(sessionId, "client_disconnect", true);
                } else {
                    log.debug("중복 DISCONNECT 무시: sessionId={}", sessionId);
                }
            } else if (!sent) {
                // 메시지 전송 실패
                log.warn("STOMP 메시지 전송 실패: sessionId={}, command={}", sessionId, command);

                if (command == StompCommand.CONNECTED) {
                    // 연결 응답 실패
                    webSocketMetricService.failConnection(sessionId, "connection_response_failed");
                }
            }
        } catch (Exception e) {
            log.error("STOMP postSend 처리 중 에러: sessionId={}, command={}", sessionId, command, e);
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
}
