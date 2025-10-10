package coffeeshout.room.infra.messaging.handler;

import coffeeshout.global.ui.WebSocketResponse;
import coffeeshout.global.websocket.LoggingSimpMessagingTemplate;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.QrCodeStatus;
import coffeeshout.room.domain.event.QrCodeCompleteEvent;
import coffeeshout.room.domain.event.RoomEventType;
import coffeeshout.room.domain.service.RoomCommandService;
import coffeeshout.room.ui.response.QrCodeStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Redis pub/sub을 통해 다른 인스턴스로부터 QR 코드 생성 완료 이벤트를 받아 처리하는 핸들러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QrCodeCompleteEventHandler implements RoomEventHandler<QrCodeCompleteEvent> {

    public static final String QR_CODE_TOPIC_TEMPLATE = "/topic/room/%s/qr-code";

    private final RoomCommandService roomCommandService;
    private final LoggingSimpMessagingTemplate messagingTemplate;

    @Override
    public void handle(QrCodeCompleteEvent event) {
        try {
            log.info("QR 코드 완료 이벤트 수신: eventId={}, joinCode={}, status={}",
                    event.eventId(), event.joinCode(), event.status());

            switch (event.status()) {
                case SUCCESS -> handleQrCodeSuccess(event);
                case ERROR -> handleQrCodeError(event);
                default -> log.error("처리할 수 없는 QR 코드 상태: eventId={}, joinCode={}, status={}",
                        event.eventId(), event.joinCode(), event.status());
            }

        } catch (Exception e) {
            log.error("QR 코드 완료 이벤트 처리 실패: eventId={}, joinCode={}",
                    event.eventId(), event.joinCode(), e);
        }
    }

    private void handleQrCodeError(QrCodeCompleteEvent event) {
        log.info("QR 코드 완료 이벤트 처리 완료 (ERROR): eventId={}, joinCode={}",
                event.eventId(), event.joinCode());

        roomCommandService.assignQrCodeError(new JoinCode(event.joinCode()));

        QrCodeStatusResponse response = new QrCodeStatusResponse(QrCodeStatus.ERROR, null);

        String destination = String.format(QR_CODE_TOPIC_TEMPLATE, event.joinCode());
        messagingTemplate.convertAndSend(destination, WebSocketResponse.success(response));

        log.debug("QR 코드 Error 이벤트 전송 완료: destination={}", destination);
    }

    private void handleQrCodeSuccess(QrCodeCompleteEvent event) {
        log.info("QR 코드 완료 이벤트 처리 완료 (SUCCESS): eventId={}, joinCode={}, url={}",
                event.eventId(), event.joinCode(), event.qrCodeUrl());

        roomCommandService.assignQrCode(new JoinCode(event.joinCode()), event.qrCodeUrl());

        QrCodeStatusResponse response = new QrCodeStatusResponse(QrCodeStatus.SUCCESS, event.qrCodeUrl());

        String destination = String.format(QR_CODE_TOPIC_TEMPLATE, event.joinCode());
        messagingTemplate.convertAndSend(destination, WebSocketResponse.success(response));

        log.debug("QR 코드 Success 이벤트 전송 완료: destination={}, url={}", destination, event.qrCodeUrl());
    }


    @Override
    public RoomEventType getSupportedEventType() {
        return RoomEventType.QR_CODE_COMPLETE;
    }
}
