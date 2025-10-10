package coffeeshout.room.domain.event;

import coffeeshout.room.domain.QrCodeStatus;
import java.time.Instant;
import java.util.UUID;

/**
 * QR 코드 생성 완료를 Redis pub/sub을 통해 모든 인스턴스에 전파하기 위한 이벤트
 */
public record QrCodeCompleteEvent(
        String eventId,
        Instant timestamp,
        RoomEventType eventType,
        String joinCode,
        QrCodeStatus status,
        String qrCodeUrl
) implements RoomBaseEvent {

    public QrCodeCompleteEvent(String joinCode, QrCodeStatus status, String qrCodeUrl) {
        this(
                UUID.randomUUID().toString(),
                Instant.now(),
                RoomEventType.QR_CODE_COMPLETE,
                joinCode,
                status,
                qrCodeUrl
        );
    }
}
