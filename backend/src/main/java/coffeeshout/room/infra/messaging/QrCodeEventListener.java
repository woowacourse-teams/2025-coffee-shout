package coffeeshout.room.infra.messaging;

import coffeeshout.global.ui.WebSocketResponse;
import coffeeshout.room.application.RoomService;
import coffeeshout.room.ui.response.QrCodeStatusResponse;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.PathMatcher;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class QrCodeEventListener {

    private static final String QR_CODE_TOPIC_PATTERN = "/topic/room/{joinCode:.{4}}/qr-code";

    private final RoomService roomService;
    private final SimpMessagingTemplate messagingTemplate;
    private final PathMatcher pathMatcher;

    @EventListener
    public void handleSubscribeQrCodeStatus(SessionSubscribeEvent event) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.wrap(event.getMessage());

        String destination = headerAccessor.getDestination();

        if (destination != null && pathMatcher.match(QR_CODE_TOPIC_PATTERN, destination)) {
            String sessionId = headerAccessor.getSessionId();
            log.info("QR 코드 상태 구독 이벤트 감지: sessionId={}, destination={}",
                    sessionId, destination);

            Map<String, String> variables = pathMatcher.extractUriTemplateVariables(QR_CODE_TOPIC_PATTERN, destination);
            String joinCode = variables.get("joinCode");

            QrCodeStatusResponse qrCodeStatus = roomService.getQrCodeStatus(joinCode);

            messagingTemplate.convertAndSendToUser(
                    Objects.requireNonNull(sessionId),
                    destination,
                    WebSocketResponse.success(qrCodeStatus)
            );

            log.debug("QR 코드 구독 시 현재 상태 전송 완료: sessionId={}, joinCode={}, status={}",
                    sessionId, joinCode, qrCodeStatus.status());
        }
    }
}
