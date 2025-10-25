package coffeeshout.room.infra.messaging.handler;

import coffeeshout.global.redis.BaseEvent;
import coffeeshout.global.redis.EventHandler;
import coffeeshout.global.ui.WebSocketResponse;
import coffeeshout.global.websocket.LoggingSimpMessagingTemplate;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.event.QrCodeStatusEvent;
import coffeeshout.room.domain.service.RoomCommandService;
import coffeeshout.room.ui.response.QrCodeStatusResponse;
import generator.annotaions.MessageResponse;
import generator.annotaions.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QrCodeStatusEventHandler implements EventHandler {

    private static final String QR_CODE_TOPIC_TEMPLATE = "/topic/room/%s/qr-code";

    private final RoomCommandService roomCommandService;
    private final LoggingSimpMessagingTemplate messagingTemplate;

    @Override
    public void handle(BaseEvent event) {
        final QrCodeStatusEvent qrEvent = (QrCodeStatusEvent) event;
        log.info("QR 코드 완료 이벤트 수신: eventId={}, joinCode={}, status={}",
                qrEvent.eventId(), qrEvent.joinCode(), qrEvent.status());

        switch (qrEvent.status()) {
            case SUCCESS -> handleQrCodeSuccess(qrEvent);
            case ERROR -> handleQrCodeError(qrEvent);
            default -> log.warn("처리할 수 없는 QR 코드 상태: eventId={}, joinCode={}, status={}",
                    qrEvent.eventId(), qrEvent.joinCode(), qrEvent.status());
        }
    }

    @Override
    public Class<?> eventType() {
        return QrCodeStatusEvent.class;
    }

    private void handleQrCodeError(QrCodeStatusEvent event) {
        log.info("QR 코드 완료 이벤트 처리 완료 (ERROR): eventId={}, joinCode={}",
                event.eventId(), event.joinCode());

        roomCommandService.assignQrCodeError(new JoinCode(event.joinCode()));

        sendQrCode(event);
    }

    private void handleQrCodeSuccess(QrCodeStatusEvent event) {
        log.info("QR 코드 완료 이벤트 처리 완료 (SUCCESS): eventId={}, joinCode={}, url={}",
                event.eventId(), event.joinCode(), event.qrCodeUrl());

        roomCommandService.assignQrCode(new JoinCode(event.joinCode()), event.qrCodeUrl());

        sendQrCode(event);
    }

    @MessageResponse(
            path = "/room/{joinCode}/qr-code",
            returnType = QrCodeStatusResponse.class
    )
    @Operation(
            summary = "QR 코드 완료 이벤트 처리",
            description = "QR 코드 처리가 완료되면 클라이언트에게 상태와 URL을 전송합니다."
    )
    private void sendQrCode(QrCodeStatusEvent event) {

        final QrCodeStatusResponse response = new QrCodeStatusResponse(event.status(), event.qrCodeUrl());

        final String destination = String.format(QR_CODE_TOPIC_TEMPLATE, event.joinCode());
        messagingTemplate.convertAndSend(destination, WebSocketResponse.success(response));

        log.debug("QR 코드 이벤트 전송 완료: destination={}, status={}, url={}",
                destination, event.status(), event.qrCodeUrl());
    }

}
