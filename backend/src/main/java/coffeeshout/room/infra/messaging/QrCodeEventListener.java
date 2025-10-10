package coffeeshout.room.infra.messaging;

import static coffeeshout.room.domain.QrCodeStatus.PENDING;

import coffeeshout.global.ui.WebSocketResponse;
import coffeeshout.global.websocket.LoggingSimpMessagingTemplate;
import coffeeshout.room.domain.event.QrCodeStatusEvent;
import coffeeshout.room.ui.response.QrCodeStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QrCodeEventListener {

    private static final String QR_CODE_TOPIC_TEMPLATE = "/topic/room/%s/qr-code";

    private final LoggingSimpMessagingTemplate messagingTemplate;

    @EventListener
    private void handleQrCodePending(QrCodeStatusEvent event) {
        if (event.status() != PENDING) {
            return;
        }

        log.info("PENDING 상태 이벤트 처리 eventId={}, joinCode={}",
                event.eventId(), event.joinCode());

        QrCodeStatusResponse response = new QrCodeStatusResponse(PENDING, null);

        String destination = String.format(QR_CODE_TOPIC_TEMPLATE, event.joinCode());
        messagingTemplate.convertAndSend(destination, WebSocketResponse.success(response));

        log.debug("QR 코드 Pending 이벤트 전송 완료: destination={}", destination);
    }
}
