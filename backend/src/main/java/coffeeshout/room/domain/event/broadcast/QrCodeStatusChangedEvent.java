package coffeeshout.room.domain.event.broadcast;

import coffeeshout.room.domain.QrCodeStatus;

/**
 * QR 코드 상태가 변경되었음을 알리는 Spring Domain Event
 * <p>
 * QR 코드 생성이 완료되거나 에러가 발생했을 때 발행됨
 * </p>
 * <p>
 * 이 이벤트를 수신하는 리스너는 WebSocket을 통해 QR 코드 상태를 브로드캐스트함
 * </p>
 */
public record QrCodeStatusChangedEvent(
        String joinCode,
        QrCodeStatus status,
        String qrCodeUrl
) {
}
