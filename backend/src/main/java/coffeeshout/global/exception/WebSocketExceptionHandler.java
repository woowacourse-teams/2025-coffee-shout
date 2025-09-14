package coffeeshout.global.exception;

import coffeeshout.global.ui.WebSocketResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
@RequiredArgsConstructor
public class WebSocketExceptionHandler {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageExceptionHandler(Exception.class)
    public void handleException(
            Exception e,
            @Header("simpSessionId") String sessionId,
            @Header("simpDestination") String destination
    ) {
        // 로그에 정확한 에러 메시지 출력
        System.out.println("WebSocket 에러 발생: " + e.getMessage());
        e.printStackTrace();

        messagingTemplate.convertAndSendToUser(
                sessionId,
                "/queue/errors",
                WebSocketResponse.error("처리 중 오류가 발생했습니다.")
        );
    }
}
